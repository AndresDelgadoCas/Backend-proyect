package fairteam_backend.adapter.out.ai;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.application.port.out.AiWorkshopPort;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

/** HTTP adapter that translates the application request to the private AI API. */
@Component
public class AiWorkshopHttpAdapter implements AiWorkshopPort {

    private final RestClient aiRestClient;
    private final String generateWorkshopPath;
    private final String aiServiceToken;

    public AiWorkshopHttpAdapter(
            RestClient aiRestClient,
            @Value("${fairteam.ai.generate-workshop-path}") String generateWorkshopPath,
            @Value("${fairteam.ai.service-token}") String aiServiceToken) {
        this.aiRestClient = aiRestClient;
        this.generateWorkshopPath = generateWorkshopPath;
        this.aiServiceToken = aiServiceToken;
    }

    @Override
    public WorkshopGenerationResponseDTO generate(
            List<MultipartFile> files,
            String subject,
            String targetLevel,
            String learningObjective,
            String learningContext,
            String generationType) {
        if (aiServiceToken == null || aiServiceToken.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "La integración interna con IA aún no está configurada.");
        }

        try {
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            for (MultipartFile file : files) {
                String filename = safeFilename(file.getOriginalFilename());
                HttpHeaders fileHeaders = new HttpHeaders();
                fileHeaders.setContentType(MediaType.parseMediaType(contentTypeFor(filename)));
                fileHeaders.setContentDisposition(ContentDisposition.formData()
                        .name("files").filename(filename).build());
                ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() { return filename; }
                };
                form.add("files", new HttpEntity<>(fileResource, fileHeaders));
            }
            form.add("subject", subject);
            form.add("targetLevel", targetLevel);
            form.add("learningObjective", learningObjective);
            form.add("learningContext", learningContext);
            form.add("generationType", generationType);

            WorkshopGenerationResponseDTO response = aiRestClient.post()
                    .uri(generateWorkshopPath)
                    .header("X-AI-Service-Token", aiServiceToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(WorkshopGenerationResponseDTO.class);

            if (response == null || response.title() == null
                    || response.activities() == null || response.assessment() == null) {
                throw new ResponseStatusException(
                        BAD_GATEWAY,
                        "El servicio de IA devolvió una respuesta incompleta.");
            }
            return response;
        } catch (IOException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "No se pudo leer el archivo enviado.");
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "El servicio de IA no está disponible en este momento.");
        } catch (RestClientResponseException ex) {
            String message = switch (ex.getStatusCode().value()) {
                case 401, 403 -> "No se pudo autenticar la conexión interna con IA. Revisa AI_SERVICE_TOKEN en Render.";
                case 413 -> "El material supera el límite permitido de 10 MB.";
                case 422 -> "No pudimos extraer texto del material. Prueba con un PDF seleccionable, DOCX o TXT.";
                case 429 -> "Gemini alcanzó el límite o la cuota disponible. Revisa la cuota de la API e inténtalo más tarde.";
                case 503 -> "El servicio de IA no está configurado o disponible. Revisa GEMINI_API_KEY y AI_SERVICE_TOKEN en Render.";
                case 502 -> upstreamGenerationMessage(ex);
                default -> "Gemini no pudo generar el taller. Revisa el modelo, la clave y la cuota de la API.";
            };
            throw new ResponseStatusException(BAD_GATEWAY, message);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "No se pudo completar la solicitud al servicio de IA.");
        }
    }

    private String upstreamGenerationMessage(RestClientResponseException ex) {
        try {
            Matcher matcher = Pattern.compile("\"detail\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
                    .matcher(ex.getResponseBodyAsString());
            if (matcher.find()) {
                String message = matcher.group(1).replace("\\\"", "\"").replace("\\n", " ").trim();
                if (!message.isEmpty() && message.length() <= 300) return message;
            }
        } catch (Exception ignored) {
            // Keep the user-facing response safe if the upstream body is not valid JSON.
        }
        return "Gemini no pudo generar el taller. Revisa el modelo, la clave y la cuota de la API.";
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) return "material";
        String normalized = originalFilename.replace('\\', '/');
        return Paths.get(normalized).getFileName().toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String contentTypeFor(String filename) {
        String normalized = filename.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".pdf")) return MediaType.APPLICATION_PDF_VALUE;
        if (normalized.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return MediaType.TEXT_PLAIN_VALUE;
    }
}

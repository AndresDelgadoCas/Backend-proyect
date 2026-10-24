package fairteam_backend.adapter.out.ai;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

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
            MultipartFile file,
            String subject,
            String targetLevel,
            String learningObjective) {
        if (aiServiceToken == null || aiServiceToken.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "La integración interna con IA aún no está configurada.");
        }

        try {
            String filename = safeFilename(file.getOriginalFilename());
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(contentTypeFor(filename)));
            fileHeaders.setContentDisposition(ContentDisposition.formData()
                    .name("file").filename(filename).build());

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("file", new HttpEntity<>(fileResource, fileHeaders));
            form.add("subject", subject);
            form.add("targetLevel", targetLevel);
            form.add("learningObjective", learningObjective);

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
            throw new ResponseStatusException(BAD_GATEWAY, "El servicio de IA no pudo procesar el material.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(BAD_GATEWAY, "No se pudo completar la solicitud al servicio de IA.");
        }
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

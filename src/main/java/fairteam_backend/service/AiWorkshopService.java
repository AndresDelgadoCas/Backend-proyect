package fairteam_backend.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class AiWorkshopService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final RestClient aiRestClient;
    private final String generateWorkshopPath;

    public AiWorkshopService(
            RestClient aiRestClient,
            @Value("${fairteam.ai.generate-workshop-path}") String generateWorkshopPath) {
        this.aiRestClient = aiRestClient;
        this.generateWorkshopPath = generateWorkshopPath;
    }

    public WorkshopGenerationResponseDTO generateWorkshop(
            MultipartFile file,
            String subject,
            String targetLevel,
            String learningObjective) {

        validate(file, subject, targetLevel, learningObjective);

        try {
            String filename = safeFilename(file.getOriginalFilename());
            String contentType = contentTypeFor(filename);

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(contentType));
            fileHeaders.setContentDisposition(ContentDisposition.formData()
                    .name("file")
                    .filename(filename)
                    .build());

            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("file", new HttpEntity<>(fileResource, fileHeaders));
            form.add("subject", subject.trim());
            form.add("targetLevel", normalizeLevel(targetLevel));
            form.add("learningObjective", learningObjective == null
                    ? ""
                    : learningObjective.trim());

            WorkshopGenerationResponseDTO response = aiRestClient.post()
                    .uri(generateWorkshopPath)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(WorkshopGenerationResponseDTO.class);

            if (response == null || response.title() == null
                    || response.activities() == null
                    || response.assessment() == null) {
                throw new ResponseStatusException(
                        BAD_GATEWAY,
                        "El servicio de IA devolvió una respuesta incompleta.");
            }

            return response;
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "No se pudo leer el archivo enviado.");
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "El servicio de IA no está disponible en este momento.");
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "El servicio de IA no pudo procesar el material.");
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "No se pudo completar la solicitud al servicio de IA.");
        }
    }

    private void validate(
            MultipartFile file,
            String subject,
            String targetLevel,
            String learningObjective) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Debes adjuntar un archivo.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "El archivo supera el límite de 10 MB.");
        }
        if (subject == null || subject.isBlank() || subject.length() > 120) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "La materia es obligatoria y no puede superar 120 caracteres.");
        }
        if (targetLevel == null || !targetLevel.trim().matches("(?i)LOW|MEDIUM|HIGH|BAJO|INTERMEDIO|ALTO")) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "El nivel debe ser LOW, MEDIUM o HIGH.");
        }

        String filename = safeFilename(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!(filename.endsWith(".pdf") || filename.endsWith(".docx")
                || filename.endsWith(".txt"))) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Formatos permitidos: PDF, DOCX y TXT.");
        }

        if (learningObjective != null && learningObjective.length() > 1000) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "El objetivo de aprendizaje no puede superar 1000 caracteres.");
        }
    }

    private String normalizeLevel(String targetLevel) {
        return switch (targetLevel.trim().toUpperCase(Locale.ROOT)) {
            case "BAJO" -> "LOW";
            case "INTERMEDIO" -> "MEDIUM";
            case "ALTO" -> "HIGH";
            default -> targetLevel.trim().toUpperCase(Locale.ROOT);
        };
    }

    private String safeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "material";
        }
        String normalized = originalFilename.replace('\\', '/');
        String basename = Paths.get(normalized).getFileName().toString();
        return basename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String contentTypeFor(String filename) {
        String normalized = filename.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF_VALUE;
        }
        if (normalized.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return MediaType.TEXT_PLAIN_VALUE;
    }
}

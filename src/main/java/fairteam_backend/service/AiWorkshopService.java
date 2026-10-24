package fairteam_backend.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.application.port.out.AiWorkshopPort;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/** Application service: validates the use case and delegates through its outbound port. */
@Service
public class AiWorkshopService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final AiWorkshopPort aiWorkshopPort;

    public AiWorkshopService(AiWorkshopPort aiWorkshopPort) {
        this.aiWorkshopPort = aiWorkshopPort;
    }

    public WorkshopGenerationResponseDTO generateWorkshop(
            MultipartFile file,
            String subject,
            String targetLevel,
            String learningObjective) {
        validate(file, subject, targetLevel, learningObjective);
        return aiWorkshopPort.generate(
                file,
                subject.trim(),
                normalizeLevel(targetLevel),
                learningObjective == null ? "" : learningObjective.trim());
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
            throw new ResponseStatusException(BAD_REQUEST, "El archivo supera el límite de 10 MB.");
        }
        if (subject == null || subject.isBlank() || subject.length() > 120) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La materia es obligatoria y no puede superar 120 caracteres.");
        }
        if (targetLevel == null
                || !targetLevel.trim().matches("(?i)LOW|MEDIUM|HIGH|BAJO|INTERMEDIO|ALTO")) {
            throw new ResponseStatusException(BAD_REQUEST, "El nivel debe ser LOW, MEDIUM o HIGH.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.toLowerCase(Locale.ROOT).endsWith(".pdf")
                || filename.toLowerCase(Locale.ROOT).endsWith(".docx")
                || filename.toLowerCase(Locale.ROOT).endsWith(".txt"))) {
            throw new ResponseStatusException(BAD_REQUEST, "Formatos permitidos: PDF, DOCX y TXT.");
        }
        if (learningObjective != null && learningObjective.length() > 1000) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
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
}

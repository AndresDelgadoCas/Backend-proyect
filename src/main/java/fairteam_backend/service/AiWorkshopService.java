package fairteam_backend.service;

import java.util.Locale;
import java.util.List;

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
            String learningObjective,
            String learningContext,
            String generationType) {
        return generateWorkshop(List.of(file), subject, targetLevel, learningObjective, learningContext, generationType, null);
    }

    public WorkshopGenerationResponseDTO generateWorkshop(
            MultipartFile file, String subject, String targetLevel, String learningObjective,
            String learningContext, String generationType, String activityStyles) {
        return generateWorkshop(List.of(file), subject, targetLevel, learningObjective, learningContext, generationType, activityStyles);
    }

    public WorkshopGenerationResponseDTO generateWorkshop(
            List<MultipartFile> files,
            String subject,
            String targetLevel,
            String learningObjective,
            String learningContext,
            String generationType,
            String activityStyles) {
        validate(files, subject, targetLevel, learningObjective, learningContext, generationType);
        String styles = normalizeStyles(activityStyles);
        return aiWorkshopPort.generate(
                files,
                subject.trim(),
                normalizeLevel(targetLevel),
                learningObjective == null ? "" : learningObjective.trim(),
                learningContext == null ? "" : learningContext.trim(),
                generationType == null ? "WORKSHOP" : generationType.trim().toUpperCase(Locale.ROOT),
                styles);
    }

    /** Kinds of work the AI service understands; it validates them again, this keeps bad input out early. */
    static final java.util.Set<String> ACTIVITY_STYLES = java.util.Set.of("GUIDED", "CODE", "APP", "DEPLOY", "CASE", "TEAM");

    static String normalizeStyles(String raw) {
        if (raw == null || raw.isBlank()) return "";
        List<String> styles = java.util.Arrays.stream(raw.split(",")).map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty()).distinct().toList();
        if (!ACTIVITY_STYLES.containsAll(styles)) {
            throw new ResponseStatusException(BAD_REQUEST, "Tipo de actividad no reconocido.");
        }
        if (styles.size() > 3) throw new ResponseStatusException(BAD_REQUEST, "Elige hasta 3 tipos de actividad.");
        return String.join(",", styles);
    }

    private void validate(
            List<MultipartFile> files,
            String subject,
            String targetLevel,
            String learningObjective,
            String learningContext,
            String generationType) {
        if (generationType != null && !generationType.trim().matches("(?i)WORKSHOP|QUIZ")) {
            throw new ResponseStatusException(BAD_REQUEST, "El tipo debe ser WORKSHOP o QUIZ.");
        }
        if (files == null || files.isEmpty() || files.size() > 6) {
            throw new ResponseStatusException(BAD_REQUEST, "Selecciona entre 1 y 6 materiales.");
        }
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(BAD_REQUEST, "Uno de los materiales está vacío.");
            }
            totalSize += file.getSize();
            if (totalSize > MAX_FILE_SIZE) {
                throw new ResponseStatusException(BAD_REQUEST, "Los materiales juntos superan el límite de 10 MB.");
            }
            String filename = file.getOriginalFilename();
            if (filename == null || !(filename.toLowerCase(Locale.ROOT).endsWith(".pdf")
                    || filename.toLowerCase(Locale.ROOT).endsWith(".docx")
                    || filename.toLowerCase(Locale.ROOT).endsWith(".txt"))) {
                throw new ResponseStatusException(BAD_REQUEST, "Formatos permitidos: PDF, DOCX y TXT.");
            }
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

        if (learningObjective != null && learningObjective.length() > 1000) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "El objetivo de aprendizaje no puede superar 1000 caracteres.");
        }
        if (learningContext != null && learningContext.length() > 2500) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "El contexto del curso no puede superar 2500 caracteres.");
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

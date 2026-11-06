package fairteam_backend.service;

import java.util.Locale;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.application.port.out.AiWorkshopPort;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.dto.WorkshopRequestDTO;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/** Application service: validates the use case and delegates through its outbound port. */
@Service
public class AiWorkshopService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final AiWorkshopPort aiWorkshopPort;

    public AiWorkshopService(AiWorkshopPort aiWorkshopPort) {
        this.aiWorkshopPort = aiWorkshopPort;
    }

    public WorkshopGenerationResponseDTO generateWorkshop(MultipartFile file, WorkshopRequestDTO request) {
        return generateWorkshop(List.of(file), request);
    }

    public WorkshopGenerationResponseDTO generateWorkshop(List<MultipartFile> files, WorkshopRequestDTO request) {
        validate(files, request);
        String type = request.generationType() == null || request.generationType().isBlank()
                ? "WORKSHOP" : request.generationType().trim().toUpperCase(Locale.ROOT);
        return aiWorkshopPort.generate(files, new WorkshopRequestDTO(
                request.subject().trim(),
                normalizeLevel(request.targetLevel()),
                trimmed(request.learningObjective()),
                trimmed(request.learningContext()),
                type,
                normalizeStyles(request.activityStyles()),
                trimmed(request.teacherPrompt())));
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
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

    static final int MAX_PROMPT = 2000;

    private void validate(List<MultipartFile> files, WorkshopRequestDTO request) {
        String generationType = request.generationType();
        String subject = request.subject();
        String targetLevel = request.targetLevel();
        String learningObjective = request.learningObjective();
        String learningContext = request.learningContext();
        if (generationType != null && !generationType.isBlank() && !generationType.trim().matches("(?i)WORKSHOP|QUIZ|CUSTOM")) {
            throw new ResponseStatusException(BAD_REQUEST, "El tipo debe ser WORKSHOP, QUIZ o CUSTOM.");
        }
        String prompt = trimmed(request.teacherPrompt());
        if (prompt.length() > MAX_PROMPT) {
            throw new ResponseStatusException(BAD_REQUEST, "Las indicaciones pueden tener máximo " + MAX_PROMPT + " caracteres.");
        }
        if (request.custom() && prompt.length() < 15) {
            throw new ResponseStatusException(BAD_REQUEST, "Describe con al menos una frase qué taller quieres.");
        }
        int count = files == null ? 0 : files.size();
        if (count > 6 || (count == 0 && !request.custom())) {
            throw new ResponseStatusException(BAD_REQUEST, "Selecciona entre 1 y 6 materiales.");
        }
        long totalSize = 0;
        for (MultipartFile file : files == null ? List.<MultipartFile>of() : files) {
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

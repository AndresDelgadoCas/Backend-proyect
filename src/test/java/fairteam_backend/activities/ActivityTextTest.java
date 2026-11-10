package fairteam_backend.activities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class ActivityTextTest {
    private static Activity.View activity(String json) {
        return new Activity.View(1, "workshop", "Taller Observer", "Patrones de comportamiento", null, "LOW",
                JsonMapper.builder().build().readTree(json), LocalDateTime.now(), 0);
    }

    @Test
    void describesTheActivityForTheAi() {
        String text = ActivityText.of(activity("""
                {"summary": "Practica Observer.", "learningObjectives": ["Implementar Observer"],
                 "activities": [{"title": "Código", "instructions": "1. Crea la interfaz", "deliverable": "Repositorio"}],
                 "assessment": [{"question": "¿Qué desacopla?"}]}"""));
        assertThat(text).contains("Actividad: Taller Observer", "Tema: Patrones de comportamiento", "Entrega: Repositorio",
                "Objetivos: Implementar Observer;", "Pregunta 1: ¿Qué desacopla?");
    }

    @Test
    void staysWithinTheLimitForHugeActivities() {
        String text = ActivityText.of(activity("{\"summary\": \"" + "x".repeat(9000) + "\"}"));
        assertThat(text).hasSizeLessThanOrEqualTo(6000);
    }
}

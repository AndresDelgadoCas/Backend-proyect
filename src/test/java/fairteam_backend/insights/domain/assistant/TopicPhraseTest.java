package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TopicPhraseTest {
    @Test
    void findsTheTopicAfterDeOrSobre() {
        assertThat(TopicPhrase.in("crea un quiz de derivadas para refuerzo")).contains("derivadas");
        assertThat(TopicPhrase.in("Prepara un taller sobre la Revolución Francesa, con fuentes")).contains("Revolución Francesa");
        assertThat(TopicPhrase.in("hazme una evaluación de ecuaciones de segundo grado")).contains("ecuaciones de segundo grado");
    }

    @Test
    void skipsWordsThatDescribeTheActivity() {
        assertThat(TopicPhrase.in("un taller de refuerzo de fracciones")).contains("fracciones");
        assertThat(TopicPhrase.in("un taller de práctica sobre el present perfect")).contains("present perfect");
        assertThat(TopicPhrase.in("hazme un taller de refuerzo para Valentina")).isEmpty();
        assertThat(TopicPhrase.in("crea un taller")).isEmpty();
    }
}

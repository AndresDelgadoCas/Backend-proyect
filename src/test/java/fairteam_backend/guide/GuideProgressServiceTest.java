package fairteam_backend.guide;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.json.JsonMapper;

class GuideProgressServiceTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void keepsOnlyKnownFieldsAndSteps() {
        var body = json.readTree("{\"open\":true,\"minimized\":\"x\",\"done\":[\"curso\",\"curso\",\"hack\",3],\"skipped\":[\"perfil\"],\"extra\":\"<script>\"}");
        var clean = GuideProgressService.clean(body);
        assertThat(clean.toString()).isEqualTo("{\"open\":true,\"minimized\":false,\"finished\":false,\"done\":[\"curso\"],\"skipped\":[\"perfil\"]}");
    }

    @Test
    void rejectsWhatIsNotAnObject() {
        assertThatThrownBy(() -> GuideProgressService.clean(json.readTree("[1,2]"))).isInstanceOf(ResponseStatusException.class);
    }
}

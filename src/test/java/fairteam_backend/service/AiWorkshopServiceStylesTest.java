package fairteam_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AiWorkshopServiceStylesTest {
    @Test
    void stylesAreNormalizedForTheAiService() {
        assertThat(AiWorkshopService.normalizeStyles(" code, deploy,code ")).isEqualTo("CODE,DEPLOY");
        assertThat(AiWorkshopService.normalizeStyles(null)).isEmpty();
        assertThat(AiWorkshopService.normalizeStyles("  ")).isEmpty();
    }

    @Test
    void unknownOrTooManyStylesAreRejected() {
        assertThatThrownBy(() -> AiWorkshopService.normalizeStyles("DANCE")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> AiWorkshopService.normalizeStyles("CODE,APP,DEPLOY,TEAM")).isInstanceOf(ResponseStatusException.class);
    }
}

package fairteam_backend.config;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTest {

    @RestController
    static class FailingController {
        @GetMapping("/ai")
        String ai() {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini alcanzó el límite o la cuota disponible.");
        }

        @GetMapping("/no-reason")
        String noReason() {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        @GetMapping("/upload")
        String upload() {
            throw new MaxUploadSizeExceededException(10);
        }
    }

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new FailingController())
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void reasonsReachTheClient() throws Exception {
        mvc.perform(get("/ai"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Gemini alcanzó el límite o la cuota disponible."));
    }

    @Test
    void missingReasonsBecomeEmptyMessages() throws Exception {
        mvc.perform(get("/no-reason"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(""))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void oversizedUploadsGetAFriendlyMessage() throws Exception {
        mvc.perform(get("/upload"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message", not(emptyString())));
    }
}

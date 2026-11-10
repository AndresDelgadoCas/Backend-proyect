package fairteam_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.application.port.out.AiWorkshopPort;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.dto.WorkshopRequestDTO;

class AiWorkshopServiceTest {
    private static final String PROMPT = "Cuatro ejercicios de JOIN con la base de datos de una tienda de barrio";
    private final AiWorkshopPort port = mock(AiWorkshopPort.class);
    private final AiWorkshopService service = new AiWorkshopService(port);

    private static WorkshopRequestDTO request(String type, String prompt) {
        return new WorkshopRequestDTO(" Bases de Datos ", "intermedio", null, null, type, null, prompt);
    }

    private static MultipartFile pdf() {
        return new MockMultipartFile("file", "tema.pdf", "application/pdf", new byte[] {1, 2, 3});
    }

    @Test
    void customRequestsGoOutWithoutMaterialAndNormalized() {
        when(port.generate(anyList(), any())).thenReturn(mock(WorkshopGenerationResponseDTO.class));
        service.generateWorkshop(List.of(), request("custom", "  " + PROMPT + " "));
        ArgumentCaptor<WorkshopRequestDTO> sent = ArgumentCaptor.forClass(WorkshopRequestDTO.class);
        verify(port).generate(org.mockito.ArgumentMatchers.eq(List.of()), sent.capture());
        assertThat(sent.getValue().generationType()).isEqualTo("CUSTOM");
        assertThat(sent.getValue().teacherPrompt()).isEqualTo(PROMPT);
        assertThat(sent.getValue().targetLevel()).isEqualTo("MEDIUM");
        assertThat(sent.getValue().subject()).isEqualTo("Bases de Datos");
    }

    @Test
    void customRequestsNeedRealInstructions() {
        assertThatThrownBy(() -> service.generateWorkshop(List.of(), request("CUSTOM", "hola")))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("al menos una frase");
        assertThatThrownBy(() -> service.generateWorkshop(List.of(pdf()), request("WORKSHOP", "x".repeat(2001))))
                .hasMessageContaining("máximo 2000");
    }

    @Test
    void withoutMaterialATopicIsNeeded() {
        assertThatThrownBy(() -> service.generateWorkshop(List.of(), request("WORKSHOP", PROMPT)))
                .hasMessageContaining("escribe el tema");
        when(port.generate(anyList(), any())).thenReturn(mock(WorkshopGenerationResponseDTO.class));
        service.generateWorkshop(List.of(), new WorkshopRequestDTO("Filosofía", "MEDIUM", "La duda metódica de Descartes", null, "QUIZ", null, null));
        assertThatThrownBy(() -> service.generateWorkshop(List.of(pdf()), request("EXAM", null)))
                .hasMessageContaining("WORKSHOP, QUIZ o CUSTOM");
    }
}

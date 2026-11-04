package fairteam_backend.insights.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import fairteam_backend.insights.adapter.out.persistence.CourseGradingSchemeEntity;
import fairteam_backend.insights.adapter.out.persistence.CourseGradingSchemeRepository;
import fairteam_backend.insights.adapter.out.persistence.JpaGradingSchemeAdapter;
import fairteam_backend.insights.domain.grading.GradingScheme;

class JpaGradingSchemeAdapterTest {
    private final CourseGradingSchemeRepository repository = mock(CourseGradingSchemeRepository.class);
    private final JpaGradingSchemeAdapter adapter = new JpaGradingSchemeAdapter(repository);

    @Test
    void storesWeightsAsCommaSeparatedText() {
        when(repository.findByCourseId(3L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        adapter.save(3, new GradingScheme(List.of(20, 30, 50), 3.0));
        ArgumentCaptor<CourseGradingSchemeEntity> saved = ArgumentCaptor.forClass(CourseGradingSchemeEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getWeights()).isEqualTo("20,30,50");
        assertThat(saved.getValue().getCourseId()).isEqualTo(3L);
    }

    @Test
    void readsBackAndIgnoresCorruptRows() {
        CourseGradingSchemeEntity row = new CourseGradingSchemeEntity();
        row.setWeights("40, 60");
        row.setPassingGrade(BigDecimal.valueOf(3.0));
        when(repository.findByCourseId(1L)).thenReturn(Optional.of(row));
        assertThat(adapter.find(1)).get().extracting(GradingScheme::weights).isEqualTo(List.of(40, 60));
        CourseGradingSchemeEntity broken = new CourseGradingSchemeEntity();
        broken.setWeights("40,40");
        broken.setPassingGrade(BigDecimal.valueOf(3.0));
        when(repository.findByCourseId(2L)).thenReturn(Optional.of(broken));
        assertThat(adapter.find(2)).isEmpty();
    }
}

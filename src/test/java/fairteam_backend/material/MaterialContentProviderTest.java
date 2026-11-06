package fairteam_backend.material;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.repository.CourseMaterialRepository;

class MaterialContentProviderTest {
    private final CourseMaterialRepository repository = mock(CourseMaterialRepository.class);
    private final MaterialContentProvider provider = new MaterialContentProvider(repository, new MaterialContentCache(1_000));

    private static CourseMaterialRepository.Metadata meta(long id) {
        return new CourseMaterialRepository.Metadata() {
            public Long getId() { return id; }
            public Long getCourseId() { return 3L; }
            public String getFileName() { return "guia.pdf"; }
            public String getContentType() { return "application/pdf"; }
            public long getFileSize() { return 2; }
            public LocalDateTime getCreatedAt() { return LocalDateTime.now(); }
        };
    }

    @Test
    void materialsOfAnotherCourseOrTeacherAreNotFound() {
        when(repository.findMetadataByIdAndCourseId(9L, 3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> provider.forCourse(3, 9)).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No se encontró el material");
    }

    @Test
    void handsOutAProxyThatReadsTheFileOnlyWhenAsked() {
        when(repository.findMetadataByIdAndCourseId(9L, 3L)).thenReturn(Optional.of(meta(9)));
        when(repository.findContentByIdAndCourseId(9L, 3L)).thenReturn(Optional.of(new byte[] {4, 2}));

        MaterialContent material = provider.forCourse(3, 9);
        assertThat(material).isInstanceOf(MaterialContentProxy.class);
        assertThat(material.fileName()).isEqualTo("guia.pdf");
        verify(repository, never()).findContentByIdAndCourseId(anyLong(), anyLong());

        assertThat(material.bytes()).containsExactly(4, 2);
        provider.forCourse(3, 9).bytes();
        verify(repository, times(1)).findContentByIdAndCourseId(9L, 3L);
        verify(repository, times(2)).findMetadataByIdAndCourseId(9L, 3L);
    }

    @Test
    void forgettingAMaterialDropsItFromTheCache() {
        when(repository.findMetadataByIdAndCourseId(9L, 3L)).thenReturn(Optional.of(meta(9)));
        when(repository.findContentByIdAndCourseId(9L, 3L)).thenReturn(Optional.of(new byte[] {4, 2}));
        provider.forCourse(3, 9).bytes();
        provider.forget(9);
        assertThat(provider.cache().size()).isZero();
    }
}

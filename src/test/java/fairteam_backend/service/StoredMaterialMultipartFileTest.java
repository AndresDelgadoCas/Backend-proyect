package fairteam_backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import fairteam_backend.material.MaterialContentCache;
import fairteam_backend.material.MaterialContentProxy;

class StoredMaterialMultipartFileTest {
    @Test
    void validationDataDoesNotReadTheFile() throws Exception {
        MaterialContentProxy proxy = new MaterialContentProxy(1, "clase.docx", "application/octet-stream", 5,
                id -> new byte[] {1, 2, 3, 4, 5}, new MaterialContentCache(100));
        StoredMaterialMultipartFile file = new StoredMaterialMultipartFile(proxy);

        assertThat(file.getOriginalFilename()).isEqualTo("clase.docx");
        assertThat(file.getSize()).isEqualTo(5);
        assertThat(file.isEmpty()).isFalse();
        assertThat(proxy.isLoaded()).isFalse();

        assertThat(file.getBytes()).hasSize(5);
        assertThat(proxy.isLoaded()).isTrue();
    }
}

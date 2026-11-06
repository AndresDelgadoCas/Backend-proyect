package fairteam_backend.material;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class MaterialContentProxyTest {
    private final AtomicInteger reads = new AtomicInteger();
    private final MaterialContentCache cache = new MaterialContentCache(1_000);

    private MaterialContentProxy proxy(long id) {
        return new MaterialContentProxy(id, "guia.pdf", "application/pdf", 3, materialId -> {
            reads.incrementAndGet();
            return new byte[] {1, 2, 3};
        }, cache);
    }

    @Test
    void metadataNeverLoadsTheFile() {
        MaterialContentProxy proxy = proxy(7);
        assertThat(proxy.fileName()).isEqualTo("guia.pdf");
        assertThat(proxy.contentType()).isEqualTo("application/pdf");
        assertThat(proxy.size()).isEqualTo(3);
        assertThat(proxy.isLoaded()).isFalse();
        assertThat(reads).hasValue(0);
    }

    @Test
    void theFileIsLoadedOnFirstUseOnly() {
        MaterialContentProxy proxy = proxy(7);
        assertThat(proxy.bytes()).containsExactly(1, 2, 3);
        proxy.bytes();
        assertThat(proxy.isLoaded()).isTrue();
        assertThat(reads).hasValue(1);
    }

    @Test
    void anotherProxyForTheSameMaterialUsesTheCache() {
        proxy(7).bytes();
        proxy(7).bytes();
        assertThat(reads).hasValue(1);
        assertThat(cache.hits()).isEqualTo(1);
    }

    @Test
    void proxyAndRealObjectShareTheSameInterface() {
        MaterialContent real = new StoredMaterialContent(7, "guia.pdf", "application/pdf", new byte[] {1, 2, 3});
        MaterialContent proxied = proxy(7);
        assertThat(proxied.bytes()).isEqualTo(real.bytes());
        assertThat(proxied.size()).isEqualTo(real.size());
    }
}

package fairteam_backend.material;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MaterialContentCacheTest {
    private static StoredMaterialContent file(long id, int bytes) {
        return new StoredMaterialContent(id, id + ".pdf", "application/pdf", new byte[bytes]);
    }

    @Test
    void evictsTheLeastRecentlyUsedFilesWhenFullByBytes() {
        MaterialContentCache cache = new MaterialContentCache(10);
        cache.put(file(1, 4));
        cache.put(file(2, 4));
        cache.get(1);                 // 1 is now the most recent
        cache.put(file(3, 4));        // 12 bytes > 10: 2 goes out
        assertThat(cache.get(2)).isEmpty();
        assertThat(cache.get(1)).isPresent();
        assertThat(cache.usedBytes()).isEqualTo(8);
    }

    @Test
    void filesBiggerThanTheCacheAreNotKept() {
        MaterialContentCache cache = new MaterialContentCache(10);
        cache.put(file(1, 11));
        assertThat(cache.size()).isZero();
    }

    @Test
    void evictionOnDeleteFreesItsBytes() {
        MaterialContentCache cache = new MaterialContentCache(10);
        cache.put(file(1, 6));
        cache.evict(1);
        assertThat(cache.get(1)).isEmpty();
        assertThat(cache.usedBytes()).isZero();
    }

    @Test
    void countsHitsAndMisses() {
        MaterialContentCache cache = new MaterialContentCache(10);
        cache.get(1);
        cache.put(file(1, 2));
        cache.get(1);
        assertThat(cache.misses()).isEqualTo(1);
        assertThat(cache.hits()).isEqualTo(1);
    }
}

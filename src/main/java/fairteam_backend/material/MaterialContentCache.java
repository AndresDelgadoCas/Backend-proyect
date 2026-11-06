package fairteam_backend.material;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Small LRU cache of material files, bounded by total bytes (not by count: one PDF can be 10 MB).
 *
 * <p>Materials are never edited, only uploaded or deleted, so an entry stays valid until
 * {@link #evict(long)} is called on delete.
 */
public class MaterialContentCache {
    private final long maxBytes;
    private final LinkedHashMap<Long, StoredMaterialContent> entries = new LinkedHashMap<>(16, 0.75f, true);
    private long usedBytes;
    private long hits;
    private long misses;

    public MaterialContentCache(long maxBytes) { this.maxBytes = Math.max(0, maxBytes); }

    public synchronized Optional<StoredMaterialContent> get(long materialId) {
        StoredMaterialContent found = entries.get(materialId);
        if (found == null) misses++; else hits++;
        return Optional.ofNullable(found);
    }

    public synchronized void put(StoredMaterialContent content) {
        if (content.size() > maxBytes) return; // bigger than the whole cache: never worth keeping
        StoredMaterialContent previous = entries.put(content.id(), content);
        if (previous != null) usedBytes -= previous.size();
        usedBytes += content.size();
        Iterator<Map.Entry<Long, StoredMaterialContent>> oldest = entries.entrySet().iterator();
        while (usedBytes > maxBytes && oldest.hasNext()) {
            usedBytes -= oldest.next().getValue().size();
            oldest.remove();
        }
    }

    public synchronized void evict(long materialId) {
        StoredMaterialContent removed = entries.remove(materialId);
        if (removed != null) usedBytes -= removed.size();
    }

    public synchronized long usedBytes() { return usedBytes; }

    public synchronized int size() { return entries.size(); }

    public synchronized long hits() { return hits; }

    public synchronized long misses() { return misses; }
}

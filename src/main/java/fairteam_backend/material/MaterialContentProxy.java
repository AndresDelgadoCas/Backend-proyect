package fairteam_backend.material;

import java.util.function.LongFunction;

/**
 * Proxy pattern, "Proxy": stands in for a material and controls access to its bytes.
 *
 * <p>Equivalent to {@code ProfileProxy} in the class example, with the three uses seen in class:
 * <ul>
 *   <li><b>Lazy loading</b>: it is built from metadata only; the PDF is read the first time
 *       {@link #bytes()} is called, never for the name, type or size.</li>
 *   <li><b>Access control</b>: it only exists for a material that the metadata query found in the
 *       requested course (and, through row level security, owned by the signed-in teacher).</li>
 *   <li><b>Cache</b>: a file read once is kept in {@link MaterialContentCache}, so generating several
 *       workshops from the same PDF does not read it from the database again.</li>
 * </ul>
 */
public final class MaterialContentProxy implements MaterialContent {
    private final long id;
    private final String fileName;
    private final String contentType;
    private final long size;
    private final LongFunction<byte[]> loader;
    private final MaterialContentCache cache;
    private StoredMaterialContent real;

    public MaterialContentProxy(long id, String fileName, String contentType, long size,
            LongFunction<byte[]> loader, MaterialContentCache cache) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.loader = loader;
        this.cache = cache;
    }

    @Override public long id() { return id; }

    @Override public String fileName() { return fileName; }

    @Override public String contentType() { return contentType; }

    @Override public long size() { return size; }

    @Override
    public synchronized byte[] bytes() {
        if (real == null) {
            // Load the real material only when it is needed, as ProfileProxy does with RealProfile.
            real = cache.get(id).orElseGet(() -> {
                StoredMaterialContent loaded = new StoredMaterialContent(id, fileName, contentType, loader.apply(id));
                cache.put(loaded);
                return loaded;
            });
        }
        return real.bytes();
    }

    /** True once the real material has been created (useful to show the lazy behaviour in tests). */
    public synchronized boolean isLoaded() { return real != null; }
}

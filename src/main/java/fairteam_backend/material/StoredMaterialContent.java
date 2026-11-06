package fairteam_backend.material;

/**
 * Proxy pattern, "Real subject": a material whose bytes are already in memory.
 *
 * <p>Like {@code RealProfile}, creating it is the costly part (the PDF is read from PostgreSQL
 * before the constructor runs); after that every call is cheap.
 */
public record StoredMaterialContent(long id, String fileName, String contentType, byte[] content) implements MaterialContent {

    public StoredMaterialContent {
        if (content == null) throw new IllegalArgumentException("El material no tiene contenido.");
    }

    @Override
    public long size() { return content.length; }

    @Override
    public byte[] bytes() { return content; }
}

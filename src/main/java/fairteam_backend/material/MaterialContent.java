package fairteam_backend.material;

/**
 * Proxy pattern, "Subject": what the rest of the backend needs from a course material.
 *
 * <p>Same role as {@code SocialMediaProfile} in the class example. Callers depend only on this
 * interface, so they cannot tell whether they hold the real file or the proxy in front of it.
 */
public interface MaterialContent {
    long id();

    String fileName();

    String contentType();

    /** Size in bytes, known from the metadata without reading the file. */
    long size();

    /** The file itself; the only operation that may be expensive. */
    byte[] bytes();
}

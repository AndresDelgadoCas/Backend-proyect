package fairteam_backend.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.material.MaterialContent;

/**
 * Lets a stored material travel the same path as an uploaded file. Name, type, size and the
 * empty check come from metadata; only reading the bytes asks the {@link MaterialContent}
 * (normally a proxy) for the file, so validation can reject a request before any PDF is read.
 */
public final class StoredMaterialMultipartFile implements MultipartFile {
    private final MaterialContent material;

    public StoredMaterialMultipartFile(MaterialContent material) { this.material = material; }
    @Override public String getName() { return "file"; }
    @Override public String getOriginalFilename() { return material.fileName(); }
    @Override public String getContentType() { return material.contentType(); }
    @Override public boolean isEmpty() { return material.size() == 0; }
    @Override public long getSize() { return material.size(); }
    @Override public byte[] getBytes() { return material.bytes(); }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(material.bytes()); }
    @Override public void transferTo(File destination) throws IOException {
        Files.write(destination.toPath(), material.bytes());
    }
}

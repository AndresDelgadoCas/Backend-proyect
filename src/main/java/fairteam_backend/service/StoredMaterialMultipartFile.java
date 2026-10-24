package fairteam_backend.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.entity.CourseMaterial;

public final class StoredMaterialMultipartFile implements MultipartFile {
    private final CourseMaterial material;

    public StoredMaterialMultipartFile(CourseMaterial material) { this.material = material; }
    @Override public String getName() { return "file"; }
    @Override public String getOriginalFilename() { return material.getFileName(); }
    @Override public String getContentType() { return material.getContentType(); }
    @Override public boolean isEmpty() { return material.getContent().length == 0; }
    @Override public long getSize() { return material.getContent().length; }
    @Override public byte[] getBytes() { return material.getContent(); }
    @Override public InputStream getInputStream() { return new ByteArrayInputStream(material.getContent()); }
    @Override public void transferTo(File destination) throws IOException {
        Files.write(destination.toPath(), material.getContent());
    }
}

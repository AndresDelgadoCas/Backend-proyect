package fairteam_backend.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.repository.CourseMaterialRepository;

/**
 * Hands out {@link MaterialContentProxy} objects. The metadata query runs every time (it is the
 * access check and is cheap); only the bytes go through the cache.
 */
@Component
public class MaterialContentProvider {
    private final CourseMaterialRepository materials;
    private final MaterialContentCache cache;

    @Autowired
    public MaterialContentProvider(CourseMaterialRepository materials,
            @Value("${fairteam.materials.cache-bytes:41943040}") long cacheBytes) {
        this(materials, new MaterialContentCache(cacheBytes));
    }

    MaterialContentProvider(CourseMaterialRepository materials, MaterialContentCache cache) {
        this.materials = materials;
        this.cache = cache;
    }

    /** A proxy for a material of the course, or 404 when it is not there for this teacher. */
    public MaterialContent forCourse(long courseId, long materialId) {
        CourseMaterialRepository.Metadata meta = materials.findMetadataByIdAndCourseId(materialId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el material en este curso."));
        return new MaterialContentProxy(meta.getId(), meta.getFileName(), meta.getContentType(), meta.getFileSize(),
                id -> materials.findContentByIdAndCourseId(id, courseId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el material en este curso.")),
                cache);
    }

    /** Called when a material is deleted so the cache never serves a file that no longer exists. */
    public void forget(long materialId) { cache.evict(materialId); }

    MaterialContentCache cache() { return cache; }
}

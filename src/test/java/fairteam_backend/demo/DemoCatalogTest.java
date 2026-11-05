package fairteam_backend.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class DemoCatalogTest {

    private final List<DemoCatalog.DemoStudent> everyone = IntStream.range(0, DemoCatalog.COURSES.size())
            .boxed().flatMap(i -> DemoCatalog.studentsOf(i).stream()).toList();

    @Test
    void coversSeveralSemesters() {
        assertThat(DemoCatalog.COURSES).hasSize(4);
        assertThat(DemoCatalog.COURSES).extracting(DemoCourse::period).containsOnly("2025-2", "2026-1", "2026-2");
        assertThat(DemoCatalog.COURSES).filteredOn(DemoCourse::inProgress).hasSize(2);
    }

    @Test
    void everyCourseHasDifferentStudents() {
        Set<String> names = new HashSet<>();
        Set<String> emails = new HashSet<>();
        everyone.forEach(s -> { names.add(s.name()); emails.add(s.email()); });
        assertThat(everyone).hasSize(92);
        assertThat(names).hasSize(92);
        assertThat(emails).hasSize(92);
    }

    @Test
    void emailsArePlainAscii() {
        assertThat(everyone).allSatisfy(s ->
                assertThat(s.email()).matches("[a-z]+\\.[a-z]+\\d{3}@estudiantes\\.demo\\.edu\\.co"));
    }

    @Test
    void everyMaterialIsBundled() {
        assertThat(DemoCatalog.COURSES).flatExtracting(DemoCourse::materials).hasSize(8)
                .allSatisfy(file -> assertThat(new ClassPathResource("demo/materials/" + file).exists()).isTrue());
    }

    @Test
    void eachCourseMixesProfiles() {
        for (int c = 0; c < DemoCatalog.COURSES.size(); c++) {
            assertThat(DemoCatalog.studentsOf(c)).extracting(DemoCatalog.DemoStudent::profile)
                    .contains(DemoProfile.STRONG, DemoProfile.FALLING, DemoProfile.STRUGGLING);
        }
    }

    @Test
    void topicsAreDifferentPerCourse() {
        assertThat(DemoCatalog.COURSES).flatExtracting(DemoCourse::topics).doesNotHaveDuplicates().hasSize(12);
    }

    @Test
    void currentCourseIsTheNewest() {
        int newest = IntStream.range(0, DemoCatalog.COURSES.size()).boxed()
                .min(java.util.Comparator.comparingInt(DemoCatalog::ageInDays)).orElseThrow();
        assertThat(DemoCatalog.COURSES.get(newest).name()).startsWith("Patrones de Diseño");
    }
}

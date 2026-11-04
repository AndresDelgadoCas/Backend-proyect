package fairteam_backend.insights.domain;

import java.util.ArrayList;
import java.util.List;

/** How many students have their average in a one-point band: [0,1), [1,2) … [4,5]. */
public record GradeBand(String label, double from, double to, int students) {

    public static List<GradeBand> of(List<StudentPerformance> performances) {
        int[] counts = new int[5];
        for (StudentPerformance p : performances) {
            if (!p.hasGrades()) continue;
            int band = (int) Math.floor(Grades.round1(p.average()));
            counts[Math.max(0, Math.min(4, band))]++;
        }
        List<GradeBand> bands = new ArrayList<>();
        for (int i = 0; i < 5; i++) bands.add(new GradeBand(i + "–" + (i + 1), i, i + 1, counts[i]));
        return List.copyOf(bands);
    }
}

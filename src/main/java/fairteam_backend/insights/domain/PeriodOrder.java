package fairteam_backend.insights.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Orders periods the way teachers write them: "Corte 1" before "Corte 2"; otherwise first-seen order. */
public final class PeriodOrder {
    private static final Pattern LAST_NUMBER = Pattern.compile("(\\d+)(?!.*\\d)");
    public static final String NO_PERIOD = "Sin periodo";

    private PeriodOrder() { }

    public static String normalize(String period) {
        return period == null || period.isBlank() ? NO_PERIOD : period.trim();
    }

    public static List<String> of(List<GradeRecord> grades) {
        List<String> seen = new ArrayList<>();
        for (GradeRecord grade : grades) {
            String period = normalize(grade.period());
            if (!seen.contains(period)) seen.add(period);
        }
        if (seen.stream().allMatch(p -> number(p) != null)) seen.sort(Comparator.comparing(PeriodOrder::number));
        return seen;
    }

    private static Integer number(String period) {
        Matcher matcher = LAST_NUMBER.matcher(period);
        return matcher.find() ? Integer.valueOf(matcher.group(1)) : null;
    }
}

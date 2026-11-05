package fairteam_backend.demo;

import java.util.List;

/**
 * One course of the demo workspace.
 *
 * @param recordedPeriods how many "cortes" already have grades; fewer than the weights means the
 *                        course is still running, so projections and the simulator have something to say
 * @param difficulty      added to every grade of the course (negative = harder course)
 * @param topics          what is graded each "corte"; they become the course's subjects in FairTeam
 */
public record DemoCourse(
        String name,
        String period,
        String description,
        List<Integer> weights,
        int recordedPeriods,
        int students,
        double difficulty,
        List<String> topics,
        List<String> materials) {

    public DemoCourse {
        if (weights.stream().mapToInt(Integer::intValue).sum() != 100) {
            throw new IllegalArgumentException("Los pesos de " + name + " deben sumar 100.");
        }
        if (recordedPeriods < 1 || recordedPeriods > weights.size()) {
            throw new IllegalArgumentException("Cortes registrados fuera de rango en " + name);
        }
        if (topics.size() != 3) throw new IllegalArgumentException("Cada curso demo califica tres temas.");
        weights = List.copyOf(weights);
        topics = List.copyOf(topics);
        materials = List.copyOf(materials);
    }

    public boolean inProgress() { return recordedPeriods < weights.size(); }
}

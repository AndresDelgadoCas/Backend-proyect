package fairteam_backend.insights.domain.assistant;

import java.time.LocalDate;

/** A saved workshop, quiz or custom activity of the course, as Búho needs it to find "el último taller de X". */
public record ActivityRef(long id, String kind, String title, String topic, LocalDate created) { }

package fairteam_backend.insights.domain;

/** One grade, already resolved to names so the domain never touches persistence. */
public record GradeRecord(long studentId, long subjectId, String subjectName, String period, double value) { }

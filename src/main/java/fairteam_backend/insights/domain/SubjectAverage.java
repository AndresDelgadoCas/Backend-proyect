package fairteam_backend.insights.domain;

public record SubjectAverage(long subjectId, String name, double average, int count, int studentsBelowPassing) { }

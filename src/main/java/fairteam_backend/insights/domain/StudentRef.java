package fairteam_backend.insights.domain;

/** A student as the analysis needs it, independent from JPA. */
public record StudentRef(long id, String name, String email, String profileLevel) { }

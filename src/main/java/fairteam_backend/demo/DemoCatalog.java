package fairteam_backend.demo;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The fixed, fictional data set: four courses of a systems engineering teacher across three semesters. */
public final class DemoCatalog {

    public static final String EMAIL_DOMAIN = "estudiantes.demo.edu.co";

    public static final List<DemoCourse> COURSES = List.of(
            new DemoCourse("Programación Orientada a Objetos — Grupo 31", "2025-2",
                    "Tercer semestre. Clases, encapsulamiento, herencia, polimorfismo e interfaces en Java.",
                    List.of(30, 30, 40), 3, 22, 0.15,
                    List.of("Clases y encapsulamiento", "Herencia y polimorfismo", "Interfaces y excepciones"),
                    List.of("poo-01-clases-objetos-encapsulamiento.pdf", "poo-02-herencia-polimorfismo-interfaces.pdf")),
            new DemoCourse("Estructuras de Datos — Grupo 42", "2026-1",
                    "Cuarto semestre. Listas, pilas, colas, árboles y análisis de complejidad.",
                    List.of(30, 30, 40), 3, 24, -0.25,
                    List.of("Listas, pilas y colas", "Árboles binarios", "Complejidad algorítmica"),
                    List.of("ed-01-listas-pilas-colas.pdf", "ed-02-arboles-binarios-busqueda.pdf")),
            new DemoCourse("Patrones de Diseño — Grupo 61", "2026-2",
                    "Sexto semestre. Patrones creacionales y de comportamiento aplicados a proyectos reales.",
                    List.of(30, 30, 40), 2, 20, 0.0,
                    List.of("Patrones creacionales", "Patrones de comportamiento", "Patrones estructurales"),
                    List.of("pd-01-patrones-creacionales.pdf", "pd-02-strategy-observer.pdf")),
            new DemoCourse("Bases de Datos I — Grupo 52", "2026-2",
                    "Quinto semestre. Modelo relacional, normalización y SQL.",
                    List.of(35, 35, 30), 2, 26, -0.1,
                    List.of("Modelo relacional", "Normalización", "Consultas SQL"),
                    List.of("bd-01-modelo-relacional-normalizacion.pdf", "bd-02-sql-join-agregacion.pdf")));

    private static final List<String> FIRST_NAMES = List.of(
            "Laura", "Santiago", "Valentina", "Mateo", "Sara", "Samuel", "Mariana", "Sebastián", "Daniela", "Nicolás",
            "Isabella", "Juan José", "Gabriela", "Tomás", "Camila", "Emiliano", "Sofía", "Andrés", "Luciana", "Felipe",
            "Manuela", "David", "Paula", "Alejandro", "Juliana", "Miguel Ángel", "Natalia", "Daniel", "Valeria", "Esteban",
            "Antonella", "Julián", "María José", "Simón", "Salomé", "Martín", "Ana Sofía", "Diego", "Catalina", "Jerónimo",
            "Laura Sofía", "Cristian", "Melissa", "Kevin", "Mariana Isabel", "Óscar", "Luisa", "Brayan", "Tatiana", "Iván",
            "Alejandra", "Carlos", "Ximena", "Jhon", "Paola", "Fabián", "Lorena", "Mauricio", "Yuliana", "Hernán");
    private static final List<String> LAST_NAMES = List.of(
            "Gómez", "Rodríguez", "Martínez", "López", "García", "Hernández", "Pérez", "Sánchez", "Ramírez", "Torres",
            "Díaz", "Vargas", "Rojas", "Moreno", "Jiménez", "Castro", "Ortiz", "Ruiz", "Álvarez", "Romero",
            "Herrera", "Medina", "Aguilar", "Castillo", "Cárdenas", "Mendoza", "Restrepo", "Osorio", "Ríos", "Quintero",
            "Muñoz", "Valencia", "Cifuentes", "Londoño", "Zapata", "Arango", "Gutiérrez", "Salazar", "Patiño", "Bermúdez",
            "Mejía", "Rincón", "Guerrero", "Escobar", "Parra", "Acosta", "Duarte", "Pineda", "Velásquez", "Benítez");

    private DemoCatalog() { }

    /** Days since each course was created: past semesters first, the current Patrones de Diseño newest. */
    private static final int[] AGE_IN_DAYS = {300, 150, 0, 1};

    public static int ageInDays(int courseIndex) { return AGE_IN_DAYS[courseIndex]; }

    /** Students of a course; indexes continue across courses so every course has different people. */
    public static List<DemoStudent> studentsOf(int courseIndex) {
        int offset = 0;
        for (int i = 0; i < courseIndex; i++) offset += COURSES.get(i).students();
        List<DemoStudent> students = new ArrayList<>();
        for (int i = 0; i < COURSES.get(courseIndex).students(); i++) {
            int n = offset + i;
            String first = FIRST_NAMES.get(n * 7 % FIRST_NAMES.size());
            String last = LAST_NAMES.get(n * 11 % LAST_NAMES.size());
            String second = LAST_NAMES.get((n * 13 + 5) % LAST_NAMES.size());
            String name = first + " " + last + " " + second;
            String email = slug(first.split(" ")[0]) + "." + slug(last) + String.format("%03d", n + 1) + "@" + EMAIL_DOMAIN;
            students.add(new DemoStudent(name, email, DemoProfile.forIndex(n)));
        }
        return students;
    }

    static String slug(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    public record DemoStudent(String name, String email, DemoProfile profile) { }
}

package fairteam_backend.insights.domain.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Everything Búho knows about using FairTeam: one entry per task, with the exact steps, the screen where it
 * happens, the control to point at and, when Búho can do it itself, the action that does it. The same text
 * goes to the AI interpreter, so free questions ("¿se pueden poner porcentajes?") get the same answers.
 */
public final class AppManual {
    /**
     * @param keys    each key is a set of word stems that must all appear ("crea curso"); the most specific match wins
     * @param spot    which control the app rings when it opens the page (see the frontend's SPOTS)
     * @param doIt    what Búho does if the teacher says "hazlo" (null when it is the teacher's own work)
     */
    public record HowTo(String id, String title, List<String> keys, String answer, String route, String vista, String spot, ReplyAction doIt) { }

    private static final List<HowTo> ENTRIES = new ArrayList<>();

    private static void add(String id, String title, String keys, String answer, String route, String vista, String spot, ReplyAction doIt) {
        List<String> stems = new ArrayList<>();
        for (String key : keys.split("\\|")) stems.add(String.join(" ", java.util.Arrays.stream(key.trim().split(" ")).map(AppManual::stem).toList()));
        ENTRIES.add(new HowTo(id, title, List.copyOf(stems), answer, route, vista, spot, doIt));
    }

    /** Verbs by their root, so "creo", "crear" and "crea" all match "cre". */
    private static String stem(String word) {
        return switch (word) {
            case "crea" -> "cre"; case "agrega" -> "agreg"; case "registra" -> "registr"; case "edita" -> "edit"; case "borra" -> "borr";
            case "elimina" -> "elimin"; case "importa" -> "import"; case "exporta" -> "export"; case "genera" -> "gener"; case "sube" -> "sub";
            case "carga" -> "carg"; case "cambia" -> "cambi"; case "califica" -> "califi"; case "organiza" -> "organi"; case "ordena" -> "orden";
            case "mueve" -> "muev"; case "descarga" -> "descarg"; case "imprime" -> "impri"; case "hacer" -> "ha"; case "prepara" -> "prepar";
            case "quita" -> "quit"; case "renombra" -> "renombr"; case "ingresa" -> "ingres"; case "anade" -> "anad"; case "arma" -> "arm";
            case "forma" -> "form"; case "pon", "poner" -> "pon"; case "simula" -> "simul"; case "escribe" -> "escrib"; case "corrige" -> "corrig";
            case "selecciona" -> "selecci"; case "elige" -> "eli"; case "anota" -> "anot"; case "notifica" -> "notific"; case "arrastra" -> "arrastr";
            case "evaluo" -> "evalu"; case "evaluar" -> "evalu"; case "calificar" -> "califi"; case "llevar" -> "llev"; case "pasar" -> "pas";
            case "publicar" -> "public"; case "enviar" -> "envi"; case "bajar" -> "baj"; case "sacar" -> "sac"; case "usar" -> "us";
            default -> word;
        };
    }

    static {
        // Courses
        add("crear-curso", "Crear un curso", "crea curso|crea asignatura|crea materia|nuevo curso|nueva asignatura|nueva materia|agrega curso|agrega materia|agrega asignatura|crea grupo",
                "Ve a Mis cursos y toca \"Crear curso\". Escribe el nombre (por ejemplo \"Cálculo I — Grupo 2\"), el periodo si quieres, y guarda. Cada curso tiene su propia lista, notas, material y talleres.",
                "cursos", null, "crear-curso", ReplyAction.of("navigate", "Ábreme el formulario", "route", "cursos", "nuevo", "1"));
        add("editar-curso", "Editar o borrar un curso", "edita curso|cambia nombre curso|borra curso|elimina curso|renombra curso|edita asignatura|elimina asignatura",
                "En Mis cursos, cada curso tiene un lápiz para editar el nombre, el periodo o la descripción, y una papelera para borrarlo. Borrar un curso quita su lista y sus notas, así que te pido confirmación.",
                "cursos", null, null, null);
        add("cambiar-curso", "Cambiar de curso", "cambia curso|otro curso|cambiar de curso|selecciona curso|elige curso",
                "Usa el selector de curso arriba a la izquierda, o dime \"cambia a\" y el nombre del curso. Todo lo demás (estudiantes, notas, talleres) se muestra del curso elegido.",
                "inicio", null, "curso", null);
        add("demo", "Cursos de demostración", "demostracion|demo|ejemplo curso|datos de prueba|cursos de prueba",
                "En Mis cursos o en Ajustes toca \"Cargar cursos de demostración\": crea cuatro cursos ficticios con estudiantes, notas, asistencia y material para que pruebes todo sin tocar tus datos.",
                "cursos", null, "demo", null);
        // Students
        add("agregar-estudiantes", "Agregar estudiantes", "agrega estudiante|registra estudiante|anade estudiante|meter estudiante|ingresa estudiante|inscribe estudiante|agrega alumno|registra alumno|lista de estudiantes",
                "En Estudiantes y notas toca \"Agregar estudiantes\" y pega la lista: un nombre por línea, y si quieres el correo después de un punto y coma (\"Ana Ruiz; ana@correo.com\"). Los repetidos se ignoran solos.",
                "estudiantes", null, "agregar-estudiantes", null);
        add("importar-lista", "Importar la lista desde Excel", "importa lista|excel lista|sube lista|carga lista|importa estudiantes|excel estudiantes",
                "En Estudiantes y notas toca \"Importar lista\" y elige tu Excel: una columna \"Nombre completo\" y, si quieres, \"Correo\". La plantilla está en Ajustes (\"Plantilla de lista\") o en \"¿Cómo debe ser el archivo de Excel?\".",
                "estudiantes", null, "importar-lista", null);
        add("editar-estudiante", "Editar o quitar un estudiante", "edita estudiante|borra estudiante|elimina estudiante|quita estudiante|cambia correo|corrige nombre",
                "En Estudiantes y notas, en la fila del estudiante, el lápiz edita su nombre o correo y la papelera lo quita del curso.",
                "estudiantes", null, null, null);
        add("ficha", "Ver la ficha de un estudiante", "ficha|detalle estudiante|historial estudiante|perfil estudiante|ver estudiante",
                "Toca el nombre del estudiante en cualquier lista: verás su promedio, la tendencia por corte, sus notas por tema, la asistencia y los apoyos registrados. Desde ahí también le escribes.",
                "estudiantes", null, null, null);
        add("apoyo", "Registrar un apoyo o intervención", "registra apoyo|intervencion|tutoria|registrar apoyo|anota apoyo",
                "Abre la ficha del estudiante y toca \"Registrar apoyo\": elige el tipo (tutoría, taller, llamada…), una nota corta y guarda. Queda en su historial.",
                "estudiantes", null, null, null);
        add("mensaje", "Escribirle a un estudiante", "mensaje|correo|escribirle|escribe estudiante|enviar correo|notifica",
                "Abre su ficha y toca \"Escribirle\", o dime \"escríbele a\" y su nombre. Te dejo un borrador según cómo va; lo revisas y lo abres en tu correo para enviarlo.",
                "estudiantes", null, null, null);
        // Grade book
        add("notas", "Poner notas", "pon nota|poner nota|ingresa nota|registra nota|califica nota|escribir nota|anota nota|libro de notas|planilla",
                "En Estudiantes y notas abre la pestaña \"Notas\", elige el corte y escribe en cada celda (con coma o punto). Las flechas y Enter te mueven entre celdas. Al final toca \"Guardar\".",
                "estudiantes", "notas", "columna", null);
        add("columnas", "Agregar u organizar columnas", "columna|agrega tema|nuevo tema|organiza tema|ordena columna|mueve columna|arrastra",
                "En la pestaña Notas toca \"+ Columna\" y escribe el tema, taller o examen. Para ordenarlas arrastra la manija ⋮⋮ de la cabecera. En el ⋯ de cada columna puedes renombrarla, eliminarla o ver todo lo del tema.",
                "estudiantes", "notas", "columna", null);
        add("porcentajes", "Porcentajes de las columnas", "porcentaje|peso columna|ponderacion|ponderar|vale cada|cuanto vale",
                "En Notas, toca \"Poner porcentajes\" y escribe el % de cada columna debajo de su nombre; deben sumar 100. Tienes \"Partes iguales\" y \"Ajustar a 100 %\". Sin porcentajes, todas pesan lo mismo.",
                "estudiantes", "notas", "porcentajes", null);
        add("cortes", "Cortes y nota definitiva", "corte|periodo|definitiva|nota final|nuevo corte|parcial",
                "En Notas cada pestaña es un corte; \"+ Nuevo corte\" agrega otro. La pestaña \"Definitiva\" calcula la nota final ponderada (por corte o por tema) y la puedes exportar. El peso de cada corte se ajusta en Seguimiento → Simulador.",
                "estudiantes", "notas", "definitiva", null);
        add("escala", "Cambiar la escala de notas", "escala|sobre 10|sobre 100|sobre 20|de 0 a|nota maxima|nota minima|aprueba con",
                "En la pestaña Notas usa \"Escala\" para elegir 0–5, 0–10, 0–20, 0–100 o la que quieras. Es por curso, y se aprueba con el 60 % de la nota máxima.",
                "estudiantes", "notas", "escala", null);
        add("importar-notas", "Importar notas desde Excel", "importa nota|excel nota|sube nota|carga nota",
                "En Estudiantes y notas toca \"Importar notas\": una hoja por corte (el nombre de la hoja es el corte), estudiantes en filas y un tema por columna. La plantilla está en Ajustes → \"Plantilla de notas\".",
                "estudiantes", null, "importar-notas", null);
        add("exportar", "Exportar a Excel", "exporta|descarga nota|excel del curso|bajar nota|sacar nota",
                "En la pestaña Notas toca \"Excel del curso\" para bajar todas las notas, o en \"Definitiva\" usa \"Exportar definitivas\". En la lista también tienes \"Exportar\".",
                "estudiantes", "notas", "excel", null);
        add("tema", "Ver todo lo de un tema", "todo del tema|todo lo del tema|talleres del tema|rubricas del tema|relacion",
                "En Notas, toca el ⋯ de la columna del tema y \"Ver todo del tema\": ves sus notas en cada corte, los talleres y quizzes que preparaste y las rúbricas que lo evalúan.",
                "estudiantes", "notas", "columna", null);
        add("asistencia", "Tomar asistencia", "asistencia|lista de clase|llamar lista|faltas|inasistencia|presentes",
                "En Estudiantes y notas abre \"Asistencia\": elige la fecha, marca Presente, Falta, Tarde o Excusa (o \"Todos presentes\") y toca \"Guardar asistencia\". Te aviso cuando alguien falta seguido.",
                "estudiantes", "asistencia", "asistencia", null);
        // Workshops
        add("taller", "Crear un taller", "crea taller|hacer taller|genera taller|taller con ia|actividad con ia|prepara taller",
                "En Talleres y quizzes elige \"Taller\", escribe el tema y, si quieres, indicaciones (\"en parejas\", \"con un caso real\"). Elige el nivel y el material (opcional) y toca \"Crear taller\". Queda guardado. O pídemelo y lo hago yo.",
                "talleres", null, "crear", ReplyAction.of("workshop", "Hazlo por mí", "mode", "workshop", "level", "MEDIUM", "auto", "true"));
        add("quiz", "Crear un quiz", "crea quiz|hacer quiz|genera quiz|evaluacion corta|crea examen|prepara examen|hacer examen",
                "En Talleres y quizzes elige \"Quiz\", escribe el tema y toca \"Crear quiz\": de 5 a 8 preguntas con su clave de respuestas. O pídemelo y lo hago yo.",
                "talleres", null, "crear", ReplyAction.of("workshop", "Hazlo por mí", "mode", "quiz", "level", "MEDIUM", "auto", "true"));
        add("a-tu-medida", "Actividad a tu medida", "a tu medida|personaliza|actividad especial|mi propio taller|prompt",
                "En Talleres y quizzes elige \"A tu medida\" y describe lo que necesitas en una o dos frases (un debate, un caso, una guía de lectura…). Lo armo así, con tu material si lo eliges.",
                "talleres", null, "crear", null);
        add("material", "Subir material", "material|subir pdf|sube pdf|archivo|libro|documento|diapositiva",
                "En Talleres y quizzes, en \"Material del curso\", toca \"Subir material nuevo\": PDF, Word (.docx) o texto. Puedes combinar hasta 6 archivos (10 MB juntos); de un libro largo tomo las páginas del tema.",
                "talleres", null, "material", null);
        add("tipo-trabajo", "Tipo de trabajo según la materia", "tipo de trabajo|area|codigo|programacion|tipo de actividad|ejercicios guiados",
                "En Talleres, \"Tipo de trabajo\" muestra solo lo que tiene sentido para tu área (problemas, análisis de fuentes, ensayo, laboratorio, código…). Detecto el área por el nombre del curso y la puedes cambiar.",
                "talleres", null, "crear", null);
        add("variantes", "Versión de refuerzo o reto", "variante|otro nivel|version de refuerzo|version de reto|version mas dificil|version mas facil",
                "Con un taller abierto, en \"Misma actividad para otro nivel\" toca Refuerzo, Seguimiento o Reto: conserva el tema y cambia la dificultad.",
                "talleres", null, null, null);
        add("descargar-taller", "Descargar o imprimir un taller", "descarga taller|imprime taller|hoja estudiante|clave docente|pdf taller|imprimir",
                "Con el taller abierto toca \"Para estudiantes\" (la hoja sin respuestas) o \"Clave docente\" (con respuestas y notas para ti). Se abre lista para imprimir o guardar como PDF.",
                "talleres", null, null, null);
        add("guardados", "Ver talleres guardados", "guardado|mis talleres|talleres anteriores|historial de talleres|donde quedan|biblioteca",
                "En Talleres y quizzes abre la pestaña \"Guardados\": están todos los del curso, con buscador. Desde ahí los abres, los editas o los borras.",
                "talleres", null, "guardados", null);
        // Rubrics
        add("rubrica", "Crear una rúbrica", "crea rubrica|hacer rubrica|nueva rubrica|genera rubrica|rubrica con ia",
                "En Rúbricas toca \"Nueva rúbrica\", elige el taller guardado que vas a evaluar (o describe la actividad) y toca \"Crear con IA\"; también puedes empezar con una plantilla. Ajusta criterios y pesos y \"Guardar rúbrica\". Si quieres, la hago yo del último taller.",
                "rubricas", null, "nueva-rubrica", ReplyAction.of("rubric", "Hazla del último taller", "target", "workshop", "latest", "true"));
        add("calificar", "Calificar con una rúbrica", "califica rubrica|evaluar rubrica|calificar con|usar rubrica|evaluo|calificar estudiante",
                "Abre la rúbrica y la pestaña \"Evaluar\": en cada criterio toca el nivel (o las teclas 1 a 4). Al completar un estudiante paso solo al siguiente; las flechas te mueven y puedes dejar un comentario.",
                "rubricas", null, "evaluar", null);
        add("llevar-libro", "Llevar notas de la rúbrica al libro", "llevar al libro|pasar nota|publicar nota|enviar nota al libro|rubrica al libro",
                "Al terminar de evaluar toca \"Llevar N notas al libro\", elige el corte y el nombre de la columna (por defecto, el del taller) y confirma. Te aviso si esa columna ya tenía notas.",
                "rubricas", null, "al-libro", null);
        add("editar-rubrica", "Editar o imprimir una rúbrica", "edita rubrica|cambia criterio|peso criterio|imprime rubrica|descarga rubrica|borra rubrica",
                "Abre la rúbrica en la pestaña \"Rúbrica\": cambias criterios, pesos (suman 100) y la descripción de cada nivel. Arriba tienes imprimir, descargar y borrar.",
                "rubricas", null, null, null);
        // Teams, follow-up, reports
        add("equipos", "Armar equipos", "equipo|grupos de trabajo|arma grupo|forma grupo|proyecto",
                "En Equipos crea un proyecto (\"Nuevo proyecto\"), elige cuántas personas por equipo y quiénes participan, y toca \"Proponer equipos\": mezclo niveles para que queden parejos. Revisa y guarda en el proyecto.",
                "equipos", null, "proyecto", null);
        add("seguimiento", "Seguimiento del curso", "seguimiento|riesgo|tendencia|mapa por tema|alerta",
                "Seguimiento tiene cuatro vistas: Prioridades (quién necesita apoyo), Tendencia (quién sube o baja entre cortes), Mapa por tema y Simulador. También sacas el informe PDF o el CSV.",
                "seguimiento", null, null, null);
        add("simulador", "Simular la nota final", "simula|cuanto necesita|proyeccion|para pasar|peso de corte|peso del corte",
                "En Seguimiento → Simulador eliges cuántos cortes tiene el semestre y cuánto pesa cada uno; te muestro cuánto necesita cada estudiante para aprobar. También puedes preguntarme \"¿qué nota necesita Ana para pasar?\".",
                "seguimiento", "simulador", null, null);
        add("informe", "Informe del curso", "informe|reporte|pdf del curso|resumen pdf",
                "En Inicio toca \"Informe del curso\" o en Seguimiento \"Informe PDF\": un resumen imprimible con promedios, quién necesita apoyo y temas difíciles. O pídemelo y lo genero.",
                "seguimiento", null, "informe", ReplyAction.of("report", "Genéralo ya"));
        add("resumen", "Resumen del día", "resumen del dia|escuchar resumen|briefing",
                "En Inicio toca \"Escuchar el resumen\" o pídemelo: te cuento cómo va el curso y qué hacer primero, en voz alta si quieres.",
                "inicio", null, null, ReplyAction.of("briefing", "Escúchalo ahora"));
        // Búho and settings
        add("buho", "Usar a Búho", "buho|asistente|ia|inteligencia artificial|hablarte|microfono|voz",
                "Ábreme con Ctrl K o con el botón Búho. Escríbeme o toca el micrófono para hablarme. Puedo responder cómo va el curso, crear talleres, quizzes y rúbricas, escribir mensajes y explicarte cómo usar cualquier parte de la app. El altavoz activa o silencia mi voz.",
                "inicio", null, "buho", null);
        add("tema-oscuro", "Tema claro u oscuro", "oscuro|modo noche|tema claro|apariencia|colores",
                "En Ajustes → Apariencia elige Claro, Oscuro o Como el sistema.",
                "ajustes", null, "apariencia", null);
        add("voz", "Velocidad de la voz", "velocidad|voz del buho|habla rapido|habla lento",
                "En Ajustes → Voz del Búho mueve la velocidad y toca \"Probar la voz\".",
                "ajustes", null, "voz", null);
        add("respaldo", "Respaldo y plantillas", "respaldo|backup|copia de seguridad|plantilla",
                "En Ajustes → Tus datos tienes \"Descargar respaldo\" y las plantillas de lista y de notas en Excel.",
                "ajustes", null, "respaldo", null);
        add("cerrar-sesion", "Cerrar sesión", "sesion|salir de la cuenta|salgo de la cuenta|logout|desconect",
                "En Ajustes toca \"Cerrar sesión\", o usa el ícono de salida junto a tu nombre abajo a la izquierda.",
                "ajustes", null, "cerrar-sesion", null);
        add("atajos", "Atajos de teclado", "atajo|teclado|tecla",
                "Pulsa ? para ver los atajos. \"g\" y una letra te lleva a una sección: g i Inicio, g e Estudiantes, g s Seguimiento, g t Talleres, g r Rúbricas, g q Equipos, g c Cursos. Ctrl K me abre.",
                "ajustes", null, null, null);
        add("tutorial", "Tutorial", "tutorial|primeros pasos|recorrido|guia|aprender a usar|como se usa|como funciona la app|como funciona la plataforma",
                "En Ajustes tienes \"Primeros pasos\" (te acompaño mientras haces cada cosa) y el \"Recorrido rápido\" (solo te muestro las secciones). Te abro Primeros pasos.",
                "ajustes", null, "primeros-pasos", ReplyAction.of("guide", "Abrir Primeros pasos"));
        add("privacidad", "Privacidad de los datos", "privacidad|datos personales|seguridad|quien ve|nombres a la ia|se envia",
                "Tus cursos solo los ves tú. A la IA le envío el material y un resumen del curso; a mí me llegan los estudiantes como códigos, sin nombres, correos ni notas individuales.",
                "ajustes", null, null, null);
    }

    private AppManual() { }

    public static List<HowTo> entries() { return List.copyOf(ENTRIES); }

    /** Words that make a request a question about how to use the app, not an order or a data question. */
    static boolean asksHow(AssistantQuery q) {
        if (q.has("como va", "como van", "como esta", "como estan", "como le fue", "como le explico", "como explico", "como les explico", "como enseno", "como le enseno")) return false;
        return q.has("como", "donde", "que es", "para que sirve", "se puede", "puedo", "ensena", "explica", "no se", "no encuentro", "no puedo",
                "de que manera", "pasos", "hay", "cuales", "cual es", "tiene", "tienes", "existe", "que se", "quien ve", "ve los", "ven los", "puede ver", "sirve", "funciona", "ayudame a", "quiero aprender", "que significa");
    }

    /** The entry whose most specific key appears whole in the question. */
    static Optional<HowTo> find(AssistantQuery q) {
        HowTo best = null;
        int bestScore = 0;
        for (HowTo entry : ENTRIES) {
            for (String key : entry.keys()) {
                String[] stems = key.trim().split(" ");
                boolean all = true;
                for (String stem : stems) if (!stem.isBlank() && !q.has(stem)) { all = false; break; }
                int score = all ? key.length() : 0;
                if (score > bestScore) { best = entry; bestScore = score; }
            }
        }
        return Optional.ofNullable(best);
    }

    /** A compact copy for the AI interpreter: title, then the steps. */
    public static String text() {
        StringBuilder out = new StringBuilder();
        for (HowTo entry : ENTRIES) out.append("- [").append(entry.id()).append("] ").append(entry.title()).append(": ").append(entry.answer()).append('\n');
        return out.toString();
    }
}

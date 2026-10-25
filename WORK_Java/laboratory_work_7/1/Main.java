import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Path DEFAULT_CATALOG = Paths.get("courses.txt");

    public static void main(String[] args) {
        Path catalog = args.length > 0 ? Paths.get(args[0]) : DEFAULT_CATALOG;
        List<Course> courses = List.of(
                new Course("Java. Базовый курс", 72),
                new Course("Алгоритмы и структуры данных", 108),
                new Course("Базы данных", 64),
                new Course("Веб-разработка", 96)
        );

        try {
            saveCatalog(catalog, courses);
            List<Course> loaded = loadCatalog(catalog);
            for (Course course : loaded) {
                System.out.println(course.name() + " — " + course.hours() + " ч.");
            }
            System.out.println("Каталог сохранён и прочитан: " + loaded.size() + " курсов");
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Ошибка каталога курсов: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void saveCatalog(Path path, List<Course> courses) throws IOException {
        requireNotDirectory(path);
        List<String> lines = new ArrayList<>();
        for (Course course : courses) {
            lines.add(course.name() + "\t" + course.hours());
        }
        Files.write(path, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private static List<Course> loadCatalog(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            throw new IOException("по пути " + path.toAbsolutePath() + " находится каталог, а не файл");
        }
        if (!Files.exists(path)) {
            throw new IOException("файл не найден: " + path.toAbsolutePath());
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Course> courses = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            if (parts.length != 2) {
                throw new IOException("строка " + (i + 1) + " имеет неверный формат");
            }
            try {
                courses.add(new Course(parts[0], Integer.parseInt(parts[1])));
            } catch (NumberFormatException e) {
                throw new IOException("строка " + (i + 1) + " содержит неверное число часов", e);
            }
        }
        return courses;
    }

    private static void requireNotDirectory(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            throw new IOException("по пути " + path.toAbsolutePath() + " находится каталог, а не файл");
        }
    }

    private record Course(String name, int hours) {
        private Course {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("название курса не должно быть пустым");
            }
            if (hours <= 0) {
                throw new IllegalArgumentException("часы курса должны быть положительными");
            }
        }
    }
}

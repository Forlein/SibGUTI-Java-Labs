/*
Задание 7. Частично повреждённый импорт

Импортёр собирает ошибки с номерами строк и не публикует половину каталога. Выберите atomic all-or-nothing либо partial result и зафиксируйте контракт типом результата.
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Path dir = null;
        boolean ok = true;

        try {
            dir = Files.createTempDirectory("import-");
            Path bad = dir.resolve("bad.csv");
            Path good = dir.resolve("good.csv");

            Files.writeString(bad, "Java;72\nАлгоритмы;сто\n;10\nБазы данных;64\n", StandardCharsets.UTF_8);
            Files.writeString(good, "Java;72\nБазы данных;64\n", StandardCharsets.UTF_8);

            CatalogStore store = new CatalogStore(List.of(new Course("Старый курс", 10)));
            System.out.println("До импорта: " + store.catalog());

            ImportResult badResult = importCatalog(bad);
            switch (badResult) {
                case ImportSuccess success -> {
                    store.publish(success.courses());
                    System.out.println("ОШИБКА: повреждённый импорт опубликован");
                    ok = false;
                }
                case ImportFailure failure -> {
                    System.out.println("Импорт отклонён, ошибок: " + failure.errors().size());
                    for (ImportError error : failure.errors()) {
                        System.out.println("  строка " + error.lineNumber() + ": " + error.message());
                    }
                }
            }

            List<Course> expectedOld = List.of(new Course("Старый курс", 10));
            System.out.println("После отклонённого импорта: " + store.catalog());
            if (!store.catalog().equals(expectedOld)) {
                System.out.println("ОШИБКА: опубликована часть каталога");
                ok = false;
            }

            ImportResult goodResult = importCatalog(good);
            switch (goodResult) {
                case ImportSuccess success -> {
                    store.publish(success.courses());
                    System.out.println("Успешный импорт опубликован: " + store.catalog());
                }
                case ImportFailure failure -> {
                    System.out.println("ОШИБКА: корректный файл отклонён: " + failure.errors());
                    ok = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка импорта: " + e.getMessage());
            ok = false;
        } finally {
            deleteRecursively(dir);
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static ImportResult importCatalog(Path path) throws IOException {
        List<Course> parsed = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] parts = line.split(";", -1);
                if (parts.length != 2) {
                    errors.add(new ImportError(lineNumber, "ожидается формат название;часы"));
                    continue;
                }

                String name = parts[0].trim();
                String hoursText = parts[1].trim();

                if (name.isEmpty()) {
                    errors.add(new ImportError(lineNumber, "пустое название"));
                    continue;
                }

                int hours;
                try {
                    hours = Integer.parseInt(hoursText);
                } catch (NumberFormatException e) {
                    errors.add(new ImportError(lineNumber, "неверное число часов: " + hoursText));
                    continue;
                }

                if (hours <= 0) {
                    errors.add(new ImportError(lineNumber, "часы должны быть положительными"));
                    continue;
                }

                parsed.add(new Course(name, hours));
            }
        }

        if (!errors.isEmpty()) {
            return new ImportFailure(List.copyOf(errors));
        }
        return new ImportSuccess(List.copyOf(parsed));
    }

    private static void deleteRecursively(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private sealed interface ImportResult permits ImportSuccess, ImportFailure {
    }

    private record ImportSuccess(List<Course> courses) implements ImportResult {
    }

    private record ImportFailure(List<ImportError> errors) implements ImportResult {
    }

    private record ImportError(int lineNumber, String message) {
    }

    private record Course(String name, int hours) {
    }

    private static final class CatalogStore {
        private List<Course> catalog;

        private CatalogStore(List<Course> initial) {
            this.catalog = List.copyOf(initial);
        }

        private void publish(List<Course> newCatalog) {
            this.catalog = List.copyOf(newCatalog);
        }

        private List<Course> catalog() {
            return catalog;
        }
    }
}

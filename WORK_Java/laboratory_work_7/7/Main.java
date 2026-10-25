/*
Задание 7. Частично повреждённый импорт

Импортёр собирает ошибки с номерами строк и не публикует половину каталога. Выберите atomic all-or-nothing либо partial result и зафиксируйте контракт типом результата.
*/
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    record Result(List<String> courses, List<String> errors) {
    }

    static Result importFile(Path path) throws IOException {
        List<String> courses = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(";");
            if (parts.length != 2 || parts[0].isBlank()) {
                errors.add("строка " + (i + 1) + ": нужно имя;часы");
                continue;
            }
            try {
                int hours = Integer.parseInt(parts[1]);
                courses.add(parts[0] + " — " + hours);
            } catch (NumberFormatException e) {
                errors.add("строка " + (i + 1) + ": неверные часы");
            }
        }

        if (!errors.isEmpty()) {
            return new Result(List.of(), errors);
        }
        return new Result(courses, List.of());
    }

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("import", ".csv");
        Files.writeString(path, "Java;72\nСети;много\n;10\n", StandardCharsets.UTF_8);

        Result result = importFile(path);
        if (!result.errors().isEmpty()) {
            System.out.println("Импорт отклонён целиком:");
            result.errors().forEach(System.out::println);
        } else {
            System.out.println("Опубликовано: " + result.courses());
        }

        Files.deleteIfExists(path);
    }
}

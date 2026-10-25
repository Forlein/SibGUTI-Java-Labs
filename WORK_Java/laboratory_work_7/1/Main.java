/*
Задание 1. Сохраните каталог курсов в UTF-8, прочитайте, обработайте отсутствие файла и существующий каталог вместо файла. Не ловите ошибку только ради печати и продолжения с ложным успехом.
*/
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("courses.txt");
        List<String> courses = List.of("Java;72", "Базы данных;64", "Сети;96");

        try {
            if (Files.isDirectory(path)) {
                throw new IOException("по пути находится каталог, а не файл");
            }
            if (!Files.exists(path)) {
                Files.write(path, courses, StandardCharsets.UTF_8);
                System.out.println("Файла не было — каталог создан");
            }
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }
}

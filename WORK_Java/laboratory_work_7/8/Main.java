/*
Задание 8. Потоковый импорт большого файла

Обработайте файл, превосходящий доступную память, без `readAllLines`. Поддержите счётчики, ограниченный список ошибок и отмену после заданного порога.
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("big", ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= 1000; i++) {
                writer.write("Курс " + i + ";" + (i % 100 == 0 ? "ошибка" : "72"));
                writer.newLine();
            }
        }

        int lines = 0;
        int good = 0;
        int errors = 0;
        List<String> firstErrors = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                String[] parts = line.split(";");
                try {
                    Integer.parseInt(parts[1]);
                    good++;
                } catch (RuntimeException e) {
                    errors++;
                    if (firstErrors.size() < 5) {
                        firstErrors.add("строка " + lines);
                    }
                }
                if (errors >= 10) {
                    System.out.println("Отмена после 10 ошибок");
                    break;
                }
            }
        }

        System.out.println("Строк: " + lines + ", корректных: " + good + ", ошибок: " + errors);
        System.out.println("Первые ошибки: " + firstErrors);
        Files.deleteIfExists(path);
    }
}

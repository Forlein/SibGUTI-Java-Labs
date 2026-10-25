/*
Задание 4. Ленивые строки

Используйте `Files.lines` только внутри try-with-resources. Воспроизведите ошибочный возврат stream наружу и объясните, кто обязан владеть ресурсом.
*/
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {
    static Stream<String> broken(Path path) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("lines", ".txt");
        Files.writeString(path, "один\nдва\n", StandardCharsets.UTF_8);

        Stream<String> stream = broken(path);
        System.out.println("Строк: " + stream.count());
        System.out.println("Stream не закрыт: владелец — вызывающий код");
        stream.close();

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            System.out.println("Правильно, строк: " + lines.count());
        }

        Files.deleteIfExists(path);
    }
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Path input = args.length > 0 ? Paths.get(args[0]) : Paths.get("input.txt");
        Path output = args.length > 1 ? Paths.get(args[1]) : Paths.get("output.txt");

        try {
            if (!Files.exists(input)) {
                Files.writeString(input, "Привет, мир!\nJava и Unicode: 😀\nТретья строка\n", StandardCharsets.UTF_8);
                System.out.println("Создан демонстрационный файл: " + input.toAbsolutePath());
            }

            Statistics statistics = process(input, output);
            System.out.printf(Locale.ROOT, "Строк: %d, UTF-16 char: %d, code points: %d%n",
                    statistics.lines(), statistics.chars(), statistics.codePoints());
            System.out.println("Граница bytes -> chars: InputStream отдаёт байты, InputStreamReader декодирует их в UTF-16 char; BufferedWriter кодирует char обратно в UTF-8 bytes.");
        } catch (IOException e) {
            System.err.println("Ошибка обработки текста: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Statistics process(Path input, Path output) throws IOException {
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
            throw new IOException("входной и выходной файлы должны различаться");
        }

        long lines = 0;
        long chars = 0;
        long codePoints = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(input), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines++;
                chars += line.length();
                codePoints += line.codePointCount(0, line.length());
                writer.write(lines + ": " + line);
                writer.newLine();
            }
            writer.flush();
        }

        return new Statistics(lines, chars, codePoints);
    }

    private record Statistics(long lines, long chars, long codePoints) {
    }
}

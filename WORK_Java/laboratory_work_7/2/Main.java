/*
Задание 2. `Reader`/`Writer`

Перепишите текстовую обработку через buffered reader/writer, считайте строки и символы. Объясните границу bytes → chars.
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path input = Paths.get("input.txt");
        Path output = Paths.get("output.txt");

        try {
            Files.writeString(input, "Привет\nJava 😀\n", StandardCharsets.UTF_8);
            int lines = 0;
            int chars = 0;
            int codePoints = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(input), StandardCharsets.UTF_8));
                 BufferedWriter writer = new BufferedWriter(
                         new OutputStreamWriter(Files.newOutputStream(output), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines++;
                    chars += line.length();
                    codePoints += line.codePointCount(0, line.length());
                    writer.write(line);
                    writer.newLine();
                }
            }

            System.out.println("Строк: " + lines + ", char: " + chars + ", code points: " + codePoints);
            System.out.println("Байты читает InputStream, символы — InputStreamReader; обратно — OutputStreamWriter.");
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }
}

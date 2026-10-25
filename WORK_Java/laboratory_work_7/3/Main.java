import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    private static final List<String> CLOSE_ORDER = new ArrayList<>();
    private static boolean readerClosed;
    private static boolean writerClosed;

    public static void main(String[] args) {
        boolean ok;
        try {
            ok = demonstrate();
        } catch (IOException e) {
            System.err.println("Ошибка демонстрации: " + e.getMessage());
            ok = false;
        }
        if (!ok) {
            System.exit(1);
        }
    }

    private static boolean demonstrate() throws IOException {
        Path dir = Files.createTempDirectory("try-with-resources-");
        Path input = dir.resolve("input.txt");
        Path output = dir.resolve("output.txt");

        try {
            Files.writeString(input, "данные для pipeline\n", StandardCharsets.UTF_8);

            try {
                runPipeline(input, output);
                System.out.println("ОШИБКА: исключение не возникло");
                return false;
            } catch (IOException e) {
                System.out.println("Перехвачено: " + e.getMessage());
            }

            System.out.println("Порядок close: " + CLOSE_ORDER);
            System.out.println("reader закрыт: " + readerClosed + ", writer закрыт: " + writerClosed);

            boolean correctOrder = CLOSE_ORDER.equals(List.of("writer.close", "reader.close"));
            if (!readerClosed || !writerClosed || !correctOrder) {
                System.err.println("ОШИБКА: ресурсы закрыты неверно");
                return false;
            }

            System.out.println("Оба ресурса закрыты; порядок обратен порядку объявления.");
            return true;
        } finally {
            Files.deleteIfExists(output);
            Files.deleteIfExists(input);
            Files.deleteIfExists(dir);
        }
    }

    private static void runPipeline(Path input, Path output) throws IOException {
        try (TrackedReader reader = new TrackedReader(Files.newBufferedReader(input, StandardCharsets.UTF_8));
             TrackedWriter writer = new TrackedWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {

            String line = reader.readLine();
            writer.write(line == null ? "" : line.toUpperCase(Locale.ROOT));
            writer.newLine();
            writer.flush();

            throw new IOException("искусственный сбой в середине pipeline");
        }
    }

    private static final class TrackedReader implements Closeable {
        private final BufferedReader delegate;

        private TrackedReader(BufferedReader delegate) {
            this.delegate = delegate;
        }

        private String readLine() throws IOException {
            return delegate.readLine();
        }

        @Override
        public void close() throws IOException {
            readerClosed = true;
            CLOSE_ORDER.add("reader.close");
            delegate.close();
        }
    }

    private static final class TrackedWriter implements Closeable {
        private final BufferedWriter delegate;

        private TrackedWriter(BufferedWriter delegate) {
            this.delegate = delegate;
        }

        private void write(String text) throws IOException {
            delegate.write(text);
        }

        private void newLine() throws IOException {
            delegate.newLine();
        }

        private void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            writerClosed = true;
            CLOSE_ORDER.add("writer.close");
            delegate.close();
        }
    }
}

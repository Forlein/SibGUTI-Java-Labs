import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Path brokenPath = null;
        Path correctPath = null;
        boolean ok = true;

        try {
            brokenPath = Files.createTempFile("lazy-lines-broken-", ".txt");
            Files.writeString(brokenPath, "первая строка\nвторая строка\nтретья строка\n", StandardCharsets.UTF_8);

            System.out.println("--- Ошибочный вариант: stream возвращён наружу ---");
            AtomicBoolean closed = new AtomicBoolean(false);
            Stream<String> leaked = brokenLines(brokenPath).onClose(() -> closed.set(true));
            System.out.println("Прочитано строк: " + leaked.count());
            System.out.println("onClose вызван после terminal operation: " + closed.get());
            System.out.println("Удаление при открытом stream: " + deleteStatus(brokenPath));
            leaked.close();
            System.out.println("onClose после leaked.close(): " + closed.get());
            System.out.println("После leaked.close(): " + deleteStatus(brokenPath));
            System.out.println("Владелец ресурса: тот, кто открыл Files.lines(); при возврате stream наружу закрывать обязан вызывающий.");

            correctPath = Files.createTempFile("lazy-lines-correct-", ".txt");
            Files.writeString(correctPath, "альфа\nбета\n", StandardCharsets.UTF_8);

            System.out.println("--- Корректный вариант: try-with-resources ---");
            long count = countLines(correctPath);
            System.out.println("Корректно посчитано строк: " + count);
            String status = deleteStatus(correctPath);
            System.out.println("После корректного try-with-resources: " + status);
            if (!status.equals("удалён")) {
                ok = false;
            }
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            ok = false;
        } finally {
            deleteQuietly(brokenPath);
            deleteQuietly(correctPath);
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static Stream<String> brokenLines(Path path) throws IOException {
        return Files.lines(path, StandardCharsets.UTF_8);
    }

    private static long countLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            return lines.count();
        }
    }

    private static String deleteStatus(Path path) {
        try {
            return Files.deleteIfExists(path) ? "удалён" : "файл уже отсутствует";
        } catch (IOException e) {
            return "не удалось (" + e.getClass().getSimpleName() + ") — дескриптор ещё открыт";
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        Path dir = null;
        boolean ok = true;

        try {
            dir = Files.createTempDirectory("atomic-replace-");
            Path target = dir.resolve("courses.txt");
            Files.writeString(target, "старый каталог\n", StandardCharsets.UTF_8);

            replaceCatalog(target, List.of("Java. Базовый курс", "Базы данных"));

            String result = Files.readString(target, StandardCharsets.UTF_8);
            System.out.println("Содержимое после замены:");
            System.out.print(result);
            String normalized = result.replace("\r\n", "\n").replace('\r', '\n');
            if (!normalized.equals("Java. Базовый курс\nБазы данных\n")) {
                System.out.println("ОШИБКА: содержимое не совпадает");
                ok = false;
            }
        } catch (IOException e) {
            System.err.println("Ошибка замены каталога: " + e.getMessage());
            ok = false;
        } finally {
            deleteRecursively(dir);
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static void replaceCatalog(Path target, List<String> lines) throws IOException {
        Path directory = target.toAbsolutePath().getParent();
        Path temp = Files.createTempFile(directory, target.getFileName() + ".", ".tmp");
        boolean moved = false;

        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            }

            moveReplacing(temp, target);
            moved = true;
        } finally {
            if (!moved) {
                Files.deleteIfExists(temp);
            }
        }
    }

    private static void moveReplacing(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            System.out.println("Замена выполнена atomic move.");
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Atomic move не поддерживается: выполнена обычная замена, атомарность не гарантирована.");
        }
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
}

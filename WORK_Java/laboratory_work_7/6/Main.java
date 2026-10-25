/*
Задание 6. Временный файл и move

Записывайте новый каталог во временный файл того же каталога, flush/close, затем заменяйте целевой файл move-операцией. Обработайте отсутствие atomic move на файловой системе.
*/
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Main {
    public static void main(String[] args) {
        Path target = Paths.get("courses.txt");
        Path temp = null;

        try {
            temp = Files.createTempFile(target.toAbsolutePath().getParent(), "courses", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write("Java\nБазы данных\n");
                writer.flush();
            }

            try {
                Files.move(temp, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Atomic move не поддерживается, выполнена обычная замена");
            }

            System.out.println(Files.readString(target, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                }
            }
        }
    }
}

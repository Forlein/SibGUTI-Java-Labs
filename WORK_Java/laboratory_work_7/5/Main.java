import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    private static final int MAGIC = 0x43415431;
    private static final int VERSION = 1;
    private static final int MAX_RECORDS = 10_000;

    public static void main(String[] args) {
        Path dir = null;
        boolean ok = true;

        try {
            dir = Files.createTempDirectory("binary-catalog-");
            Path valid = dir.resolve("valid.bin");
            Path badVersion = dir.resolve("bad-version.bin");
            Path truncated = dir.resolve("truncated.bin");

            List<Course> courses = List.of(
                    new Course("Java. Базовый курс", 72),
                    new Course("Алгоритмы и структуры данных", 108),
                    new Course("Базы данных", 64)
            );

            writeCatalog(valid, courses);
            List<Course> loaded = readCatalog(valid);
            System.out.println("Прочитан каталог: " + loaded);

            writeRawHeader(badVersion, MAGIC, 99, 0);
            ok &= expectRejected(badVersion, "неизвестная версия");

            writeCatalog(truncated, courses);
            try (RandomAccessFile file = new RandomAccessFile(truncated.toFile(), "rw")) {
                file.setLength(Math.max(0, file.length() - 2));
            }
            ok &= expectRejected(truncated, "обрезанный файл");
        } catch (IOException e) {
            System.err.println("Ошибка бинарного формата: " + e.getMessage());
            ok = false;
        } finally {
            deleteRecursively(dir);
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static void writeCatalog(Path path, List<Course> courses) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(courses.size());
            for (Course course : courses) {
                out.writeUTF(course.name());
                out.writeInt(course.hours());
            }
        }
    }

    private static List<Course> readCatalog(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {
            try {
                int magic = in.readInt();
                if (magic != MAGIC) {
                    throw new InvalidCatalogException("неверный magic header");
                }

                int version = in.readInt();
                if (version != VERSION) {
                    throw new InvalidCatalogException("неизвестная версия: " + version);
                }

                int count = in.readInt();
                if (count < 0 || count > MAX_RECORDS) {
                    throw new InvalidCatalogException("недопустимое количество записей: " + count);
                }

                List<Course> courses = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    String name = in.readUTF();
                    int hours = in.readInt();
                    if (name.isBlank()) {
                        throw new InvalidCatalogException("пустое название в записи #" + (i + 1));
                    }
                    if (hours <= 0) {
                        throw new InvalidCatalogException("неверные часы в записи #" + (i + 1));
                    }
                    courses.add(new Course(name, hours));
                }

                if (in.read() != -1) {
                    throw new InvalidCatalogException("лишние данные после записей");
                }
                return courses;
            } catch (EOFException e) {
                throw new InvalidCatalogException("файл обрезан", e);
            } catch (UTFDataFormatException e) {
                throw new InvalidCatalogException("повреждённая строка в файле", e);
            }
        }
    }

    private static void writeRawHeader(Path path, int magic, int version, int count) throws IOException {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(magic);
            out.writeInt(version);
            out.writeInt(count);
        }
    }

    private static boolean expectRejected(Path path, String label) {
        try {
            List<Course> courses = readCatalog(path);
            System.out.println("ОШИБКА: " + label + " был принят: " + courses);
            return false;
        } catch (InvalidCatalogException e) {
            System.out.println(label + " отклонён: " + e.getMessage());
            return true;
        } catch (IOException e) {
            System.out.println(label + " отклонён (I/O): " + e.getMessage());
            return true;
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

    private record Course(String name, int hours) {
    }

    private static final class InvalidCatalogException extends IOException {
        private static final long serialVersionUID = 1L;

        private InvalidCatalogException(String message) {
            super(message);
        }

        private InvalidCatalogException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

/*
Задание 9. Версионируемый формат

Добавьте schema version, checksum и migration из версии 1 в 2. Неизвестная будущая версия не читается как старая. Повреждение обнаруживается до публикации объектов.
*/
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.CRC32;

public class Main {
    private static final int MAGIC = 0x4341544C;
    private static final int VERSION_1 = 1;
    private static final int VERSION_2 = 2;
    private static final int MAX_PAYLOAD = 64 * 1024 * 1024;
    private static final int MAX_RECORDS = 100_000;

    public static void main(String[] args) {
        Path dir = null;
        boolean ok = true;

        try {
            dir = Files.createTempDirectory("versioned-format-");
            List<Course> source = List.of(
                    new Course("Java. Базовый курс", 72, 2),
                    new Course("Базы данных", 108, 3)
            );

            Path v2 = dir.resolve("catalog-v2.bin");
            writeCatalog(v2, VERSION_2, source);

            Publisher publisher = new Publisher(List.of(new Course("Старый курс", 36, 1)));
            System.out.println("До публикации: " + publisher.catalog());

            LoadedCatalog loadedV2 = readCatalog(v2);
            publisher.publish(loadedV2.courses());
            System.out.println("v2 прочитан (версия " + loadedV2.sourceVersion() + "): " + publisher.catalog());

            Path v1 = dir.resolve("catalog-v1.bin");
            writeCatalog(v1, VERSION_1, source);
            LoadedCatalog loadedV1 = readCatalog(v1);
            System.out.println("v1 миграция -> v2 (версия " + loadedV1.sourceVersion() + "): " + loadedV1.courses());

            Path future = dir.resolve("catalog-v3.bin");
            writeRawVersion(future, 3);
            ok &= expectRejected(future, "будущая версия");

            Path corrupted = dir.resolve("catalog-corrupted.bin");
            byte[] bytes = Files.readAllBytes(v2);
            bytes[bytes.length - 1] ^= 0x5A;
            Files.write(corrupted, bytes);

            List<Course> before = publisher.catalog();
            ok &= expectRejected(corrupted, "повреждённый файл");
            if (!publisher.catalog().equals(before)) {
                System.out.println("ОШИБКА: повреждённый файл изменил опубликованный каталог");
                ok = false;
            } else {
                System.out.println("Опубликованный каталог не изменён после повреждения.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка версионируемого формата: " + e.getMessage());
            ok = false;
        } finally {
            deleteRecursively(dir);
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static void writeCatalog(Path path, int version, List<Course> courses) throws IOException {
        if (version != VERSION_1 && version != VERSION_2) {
            throw new IllegalArgumentException("неподдерживаемая версия для записи: " + version);
        }

        ByteArrayOutputStream payloadBytes = new ByteArrayOutputStream();
        try (DataOutputStream payload = new DataOutputStream(payloadBytes)) {
            payload.writeInt(courses.size());
            for (Course course : courses) {
                payload.writeUTF(course.name());
                payload.writeInt(course.hours());
                if (version == VERSION_2) {
                    payload.writeInt(course.credits());
                }
            }
        }

        byte[] payload = payloadBytes.toByteArray();
        CRC32 crc = new CRC32();
        crc.update(payload);

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(MAGIC);
            out.writeInt(version);
            out.writeInt(payload.length);
            out.writeInt((int) crc.getValue());
            out.write(payload);
        }
    }

    private static LoadedCatalog readCatalog(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(path)))) {

            int magic;
            int version;
            int payloadLength;
            int expectedChecksum;

            try {
                magic = in.readInt();
                if (magic != MAGIC) {
                    throw new CatalogFormatException("неверный magic header");
                }

                version = in.readInt();
                if (version != VERSION_1 && version != VERSION_2) {
                    throw new UnsupportedVersionException(version);
                }

                payloadLength = in.readInt();
                if (payloadLength < 0 || payloadLength > MAX_PAYLOAD) {
                    throw new CatalogFormatException("недопустимая длина payload: " + payloadLength);
                }

                expectedChecksum = in.readInt();
            } catch (EOFException e) {
                throw new CatalogFormatException("заголовок файла обрезан", e);
            }

            byte[] payload = in.readNBytes(payloadLength);
            if (payload.length != payloadLength) {
                throw new CatalogFormatException("payload обрезан: ожидалось "
                        + payloadLength + " байт, получено " + payload.length);
            }

            if (in.read() != -1) {
                throw new CatalogFormatException("лишние данные после payload");
            }

            CRC32 crc = new CRC32();
            crc.update(payload);
            if ((int) crc.getValue() != expectedChecksum) {
                throw new CatalogFormatException("checksum не совпал: данные повреждены");
            }

            return parsePayload(version, payload);
        }
    }

    private static LoadedCatalog parsePayload(int version, byte[] payload) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload))) {
            int count;
            try {
                count = in.readInt();
            } catch (EOFException e) {
                throw new CatalogFormatException("payload обрезан: нет количества записей", e);
            }

            if (count < 0 || count > MAX_RECORDS) {
                throw new CatalogFormatException("недопустимое количество записей: " + count);
            }

            List<Course> courses = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                try {
                    String name = in.readUTF();
                    int hours = in.readInt();
                    int credits = version == VERSION_1 ? creditsFromHours(hours) : in.readInt();
                    validateCourse(name, hours, credits, i + 1);
                    courses.add(new Course(name, hours, credits));
                } catch (EOFException e) {
                    throw new CatalogFormatException("payload обрезан в записи #" + (i + 1), e);
                } catch (UTFDataFormatException e) {
                    throw new CatalogFormatException("повреждённая строка в записи #" + (i + 1), e);
                }
            }

            if (in.available() != 0) {
                throw new CatalogFormatException("после записей остались лишние байты");
            }

            return new LoadedCatalog(version, List.copyOf(courses));
        }
    }

    private static int creditsFromHours(int hours) {
        return Math.max(1, hours / 36);
    }

    private static void validateCourse(String name, int hours, int credits, int index)
            throws CatalogFormatException {
        if (name == null || name.isBlank()) {
            throw new CatalogFormatException("пустое название в записи #" + index);
        }
        if (hours <= 0) {
            throw new CatalogFormatException("неверные часы в записи #" + index);
        }
        if (credits <= 0) {
            throw new CatalogFormatException("неверные кредиты в записи #" + index);
        }
    }

    private static void writeRawVersion(Path path, int version) throws IOException {
        byte[] payload = new byte[] {0, 0, 0, 0};
        CRC32 crc = new CRC32();
        crc.update(payload);

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(MAGIC);
            out.writeInt(version);
            out.writeInt(payload.length);
            out.writeInt((int) crc.getValue());
            out.write(payload);
        }
    }

    private static boolean expectRejected(Path path, String label) {
        try {
            LoadedCatalog catalog = readCatalog(path);
            System.out.println("ОШИБКА: " + label + " принят: " + catalog);
            return false;
        } catch (UnsupportedVersionException e) {
            System.out.println(label + " отклонён: " + e.getMessage());
            return true;
        } catch (CatalogFormatException e) {
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

    private record Course(String name, int hours, int credits) {
    }

    private record LoadedCatalog(int sourceVersion, List<Course> courses) {
    }

    private static class CatalogFormatException extends IOException {
        private static final long serialVersionUID = 1L;

        private CatalogFormatException(String message) {
            super(message);
        }

        private CatalogFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static final class UnsupportedVersionException extends CatalogFormatException {
        private static final long serialVersionUID = 1L;

        private UnsupportedVersionException(int version) {
            super("неизвестная версия формата: " + version);
        }
    }

    private static final class Publisher {
        private List<Course> catalog;

        private Publisher(List<Course> initial) {
            this.catalog = List.copyOf(initial);
        }

        private void publish(List<Course> newCatalog) {
            this.catalog = List.copyOf(newCatalog);
        }

        private List<Course> catalog() {
            return catalog;
        }
    }
}

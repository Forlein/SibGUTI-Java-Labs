/*
Задание 9. Версионируемый формат

Добавьте schema version, checksum и migration из версии 1 в 2. Неизвестная будущая версия не читается как старая. Повреждение обнаруживается до публикации объектов.
*/
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class Main {
    static final int MAGIC = 0xCAFE;

    record Course(String name, int hours, int credits) {
    }

    static byte[] payload(int version, List<Course> courses) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(courses.size());
            for (Course course : courses) {
                out.writeUTF(course.name());
                out.writeInt(course.hours());
                if (version == 2) {
                    out.writeInt(course.credits());
                }
            }
        }
        return bytes.toByteArray();
    }

    static void write(Path path, int version, List<Course> courses) throws IOException {
        byte[] data = payload(version, courses);
        CRC32 crc = new CRC32();
        crc.update(data);

        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeInt(MAGIC);
            out.writeInt(version);
            out.writeInt(data.length);
            out.writeInt((int) crc.getValue());
            out.write(data);
        }
    }

    static List<Course> read(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
            if (in.readInt() != MAGIC) {
                throw new IOException("неверный magic");
            }
            int version = in.readInt();
            if (version != 1 && version != 2) {
                throw new IOException("неизвестная версия: " + version);
            }
            int length = in.readInt();
            int checksum = in.readInt();
            byte[] data = in.readNBytes(length);

            CRC32 crc = new CRC32();
            crc.update(data);
            if ((int) crc.getValue() != checksum) {
                throw new IOException("checksum не совпал");
            }

            try (DataInputStream payload = new DataInputStream(new ByteArrayInputStream(data))) {
                int count = payload.readInt();
                List<Course> courses = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    String name = payload.readUTF();
                    int hours = payload.readInt();
                    int credits = version == 1 ? Math.max(1, hours / 36) : payload.readInt();
                    courses.add(new Course(name, hours, credits));
                }
                return courses;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("catalog", ".bin");
        List<Course> courses = List.of(new Course("Java", 72, 2));

        write(path, 1, courses);
        System.out.println("v1 -> v2: " + read(path));

        write(path, 2, courses);
        System.out.println("v2: " + read(path));

        byte[] bad = Files.readAllBytes(path);
        bad[bad.length - 1] ^= 1;
        Files.write(path, bad);
        try {
            read(path);
        } catch (IOException e) {
            System.out.println("Повреждение: " + e.getMessage());
        }

        Files.deleteIfExists(path);
    }
}

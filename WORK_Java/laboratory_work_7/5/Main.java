/*
Задание 5. Бинарный формат

Запишите magic header, version, количество и записи через `DataOutputStream`; прочитайте через `DataInputStream`. Отклоняйте неизвестную версию и обрезанный файл.
*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    static final int MAGIC = 0x12345678;
    static final int VERSION = 1;

    record Course(String name, int hours) {
    }

    public static void main(String[] args) {
        Path path = Paths.get("courses.bin");
        List<Course> courses = List.of(new Course("Java", 72), new Course("Сети", 96));

        try {
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
                out.writeInt(MAGIC);
                out.writeInt(VERSION);
                out.writeInt(courses.size());
                for (Course course : courses) {
                    out.writeUTF(course.name());
                    out.writeInt(course.hours());
                }
            }

            try (DataInputStream in = new DataInputStream(Files.newInputStream(path))) {
                if (in.readInt() != MAGIC) {
                    throw new IOException("неверный magic header");
                }
                int version = in.readInt();
                if (version != VERSION) {
                    throw new IOException("неизвестная версия: " + version);
                }
                int count = in.readInt();
                for (int i = 0; i < count; i++) {
                    System.out.println(in.readUTF() + " — " + in.readInt());
                }
            }
        } catch (EOFException e) {
            System.err.println("Файл обрезан");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }
}

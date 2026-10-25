/*
Задание 3. Try-with-resources

Создайте pipeline чтения и записи, в середине которого возникает исключение. Докажите закрытие обоих ресурсов и правильный порядок close.
*/
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static final List<String> CLOSED = new ArrayList<>();

    static class Resource implements Closeable {
        final String name;

        Resource(String name) {
            this.name = name;
        }

        String read() {
            return "данные";
        }

        void write(String text) {
            System.out.println("Записано: " + text);
        }

        @Override
        public void close() {
            CLOSED.add(name);
        }
    }

    public static void main(String[] args) {
        try (Resource reader = new Resource("reader");
             Resource writer = new Resource("writer")) {
            writer.write(reader.read());
            throw new IOException("сбой в середине pipeline");
        } catch (IOException e) {
            System.out.println("Исключение: " + e.getMessage());
        }
        System.out.println("Порядок close: " + CLOSED);
    }
}

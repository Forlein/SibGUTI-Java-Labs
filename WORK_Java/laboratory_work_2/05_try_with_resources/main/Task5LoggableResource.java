package ru.sibguti.lab2.task05;

/*
 * Задание 5. Try-with-resources (часть 1).
 * Класс Task5LoggableResource - тестовый ресурс, который пишет лог при
 * открытии, использовании и закрытии. Он нужен, чтобы увидеть порядок
 * этих действий при успехе, при исключении в работе и при ошибке в close.
 * Класс реализует AutoCloseable, поэтому try-with-resources вызывает close()
 * сам - и при нормальном выходе, и при исключении, и при return.
 * Зависимости: использует только java.lang.AutoCloseable и System.out;
 * его создаёт и использует Task5TryWithResources.
 */
public class Task5LoggableResource implements AutoCloseable {

    private final String name;
    private final boolean failOnUse;
    private final boolean failOnClose;
    private boolean closed;

    public Task5LoggableResource(String name, boolean failOnUse, boolean failOnClose) {
        this.name = name;
        this.failOnUse = failOnUse;
        this.failOnClose = failOnClose;
        this.closed = false;
        log("открыт  : " + name);
    }

    public void use(String payload) throws Exception {
        log("использован: " + name + ", данные=\"" + payload + "\"");
        if (failOnUse) {
            throw new Exception("ошибка при работе с ресурсом " + name);
        }
    }

    public void close() throws Exception {
        if (!closed) {
            closed = true;
            log("закрыт  : " + name);
        }
        if (failOnClose) {
            throw new Exception("ошибка при закрытии ресурса " + name);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    /*
     * Метод log печатает одну строку лога ресурса в едином формате,
     * чтобы было видно, в каком порядке происходили open, use и close.
     * Его вызывают конструктор, use и close.
     */
    private void log(String message) {
        System.out.println("   [ресурс] " + message);
    }
}

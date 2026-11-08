/*
Задание 5. Try-with-resource
Создайте тестовый `AutoCloseable`, логирующий open/use/close. 
Покажите закрытие при успехе и исключении. Затем создайте ошибку 
и в работе, и в `close`, исследуйте suppressed exceptions.
*/
public class Main {

    static class Resource implements AutoCloseable {
        final String name;
        final boolean failOnUse, failOnClose;
        Resource(String name, boolean failOnUse, boolean failOnClose) {
            this.name = name; this.failOnUse = failOnUse; this.failOnClose = failOnClose;
            System.out.println("open " + name);
        }
        void use() {
            System.out.println("use " + name);
            if (failOnUse) throw new RuntimeException("use fail " + name);
        }
        @Override public void close() {
            System.out.println("close " + name);
            if (failOnClose) throw new RuntimeException("close fail " + name);
        }
    }

    public static void main(String[] args) {
        System.out.println("--- success ---");
        try (Resource r = new Resource("A", false, false)) { r.use(); }

        System.out.println("--- fail on use ---");
        try (Resource r = new Resource("B", true, false)) { r.use(); }
        catch (RuntimeException e) { System.out.println("caught: " + e.getMessage()); }

        System.out.println("--- fail on use AND close ---");
        try (Resource r = new Resource("C", true, true)) { r.use(); }
        catch (RuntimeException e) {
            System.out.println("caught: " + e.getMessage());
            for (Throwable s : e.getSuppressed()) System.out.println("suppressed: " + s.getMessage());
        }
    }
}

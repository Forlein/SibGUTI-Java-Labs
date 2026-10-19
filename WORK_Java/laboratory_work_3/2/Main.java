// 1. Базовый класс
class NotificationSender {
    public void send(Object msg) {
        System.out.println("Base.send(Object): " + msg);
    }

    public void send(String msg) {
        System.out.println("Base.send(String): " + msg);
    }
}

// 2. Наследник Email
class EmailSender extends NotificationSender {
    @Override
    public void send(String msg) {
        System.out.println("Email.send(String): " + msg);
    }
}

// 3. Наследник Console
class ConsoleSender extends NotificationSender {
    @Override
    public void send(String msg) {
        System.out.println("Console.send(String): " + msg);
    }
}

// 4. Запуск
public class Main {
    public static void main(String[] args) {
        NotificationSender s1 = new EmailSender();
        NotificationSender s2 = new ConsoleSender();
        EmailSender e = new EmailSender();

        System.out.println("--- 1. Обычный String ---");
        s1.send("hello");
        s2.send("hello");
        e.send("hello");

        System.out.println("--- 2. Число int (упаковывается в Object) ---");
        s1.send(42);
        e.send(42);

        System.out.println("--- 3. Явное приведение к Object ---");
        s1.send((Object) "hello");
        e.send((Object) "hello");
    }
}
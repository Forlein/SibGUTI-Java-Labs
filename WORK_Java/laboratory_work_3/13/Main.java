// 1. Интерфейс (единый контракт)
interface NotificationSender {
    void send(String message);
}

// 2. Реализации
class EmailSender implements NotificationSender {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

class SmsSender implements NotificationSender {
    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

class PushSender implements NotificationSender {
    @Override
    public void send(String message) {
        System.out.println("Push: " + message);
    }
}

// 3. Клиент (никаких switch)
class NotificationService {
    public void send(NotificationSender sender, String message) {
        sender.send(message);
    }
}

// 4. Запуск
public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        service.send(new EmailSender(), "Привет");
        service.send(new SmsSender(), "Привет");
        service.send(new PushSender(), "Привет");
    }
}
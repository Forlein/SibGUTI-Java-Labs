// 1. Контракт
interface NotificationSender {
    void send(String message);
}

// 2. Базовая реализация (тот самый EmailSender)
class EmailSender implements NotificationSender {
    @Override
    public void send(String message) {
        System.out.println("  [EmailSender] Письмо отправлено: " + message);
    }
}

// 3. Декоратор Логирования (Logging)
class LoggingSender implements NotificationSender {
    private final NotificationSender wrapped; // Тот, кого мы оборачиваем

    public LoggingSender(NotificationSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message) {
        System.out.println("[LOG] Начинаю отправку: " + message);
        wrapped.send(message); // Делегируем работу внутреннему объекту
        System.out.println("[LOG] Отправка завершена.");
    }
}

// 4. Декоратор Повторов (Retry)
class RetrySender implements NotificationSender {
    private final NotificationSender wrapped;

    public RetrySender(NotificationSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void send(String message) {
        int attempts = 3;
        for (int i = 1; i <= attempts; i++) {
            try {
                wrapped.send(message);
                return; // Если успешно, выходим
            } catch (Exception e) {
                System.out.println("[RETRY] Попытка " + i + " не удалась. Пробую снова...");
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        NotificationSender baseEmail = new EmailSender();

        System.out.println("--- Порядок 1: Сначала Логирование, потом Retry ---");
        // Читаем справа налево: Retry оборачивает Email, Логирование оборачивает Retry
        NotificationSender order1 = new LoggingSender(new RetrySender(baseEmail));
        order1.send("Привет");

        System.out.println("\n--- Порядок 2: Сначала Retry, потом Логирование ---");
        // Retry оборачивает (Логирование оборачивает Email)
        NotificationSender order2 = new RetrySender(new LoggingSender(baseEmail));
        order2.send("Привет");
    }
}
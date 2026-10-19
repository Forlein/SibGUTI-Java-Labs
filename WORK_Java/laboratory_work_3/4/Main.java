// 1. Абстрактный базовый класс (каркас)
abstract class AbstractNotificationSender {
    
    // ПУБЛИЧНЫЙ АЛГОРИТМ. Он final, чтобы наследники его не сломали.
    public final void send(String address, String message) {
        // Шаг 1: Общая проверка (её пишем ОДИН раз здесь)
        if (address == null || address.isBlank()) {
            System.out.println("Ошибка: адресат не указан!");
            return;
        }
        
        // Шаг 2: Специфичная отправка (её делают наследники)
        doSend(address, message);
        
        // Шаг 3: Еще одна общая часть (например, логирование)
        System.out.println("Лог: сообщение для " + address + " обработано.");
    }

    // АБСТРАКТНЫЙ МЕТОД. Точка расширения. У него нет тела, только контракт.
    protected abstract void doSend(String address, String message);
}

// 2. Реализация для Email
class EmailSender extends AbstractNotificationSender {
    @Override
    protected void doSend(String address, String message) {
        // Тут только специфика Email. Проверку адресата писать НЕ НУЖНО.
        System.out.println("Email отправлен на " + address + ": " + message);
    }
}

// 3. Реализация для Console
class ConsoleSender extends AbstractNotificationSender {
    @Override
    protected void doSend(String address, String message) {
        // Тут только специфика Console.
        System.out.println("Вывод в консоль [" + address + "]: " + message);
    }
}

// 4. Проверка
public class Main {
    public static void main(String[] args) {
        AbstractNotificationSender email = new EmailSender();
        AbstractNotificationSender console = new ConsoleSender();

        System.out.println("--- Тест 1: нормальный адрес ---");
        email.send("user@mail.com", "Привет");
        console.send("admin", "Система запущена");

        System.out.println("\n--- Тест 2: пустой адрес (проверка родителя) ---");
        email.send("", "Это сообщение не должно уйти");
    }
}
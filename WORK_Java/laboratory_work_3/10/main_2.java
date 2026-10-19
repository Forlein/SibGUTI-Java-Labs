interface EmailNotifier {
    default void send() {
        System.out.println("Отправка по Email");
    }
}

interface SmsNotifier {
    default void send() {
        System.out.println("Отправка по SMS");
    }
}

// МЫ ЯВНО РАЗРЕШАЕМ КОНФЛИКТ
class MultiNotifier implements EmailNotifier, SmsNotifier {
    @Override
    public void send() {
        // Явно указываем, чьи методы вызывать. Можно оба, можно один.
        EmailNotifier.super.send();
        SmsNotifier.super.send();
    }
}

public class main_2 {
    public static void main(String[] args) {
        new MultiNotifier().send();
    }
}
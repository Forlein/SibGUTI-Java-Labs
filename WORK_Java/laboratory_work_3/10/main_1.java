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

// КОМПИЛЯТОР ЗДЕСЬ ОСТАНОВИТСЯ С ОШИБКОЙ
class MultiNotifier implements EmailNotifier, SmsNotifier {
}

public class main_1 {
    public static void main(String[] args) {
        new MultiNotifier().send();
    }
}
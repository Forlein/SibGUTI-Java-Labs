package tasks;

import common.Notification;
import common.NotificationSender;

/**
 * Задание 4. Абстрактный базовый класс.
 * Создайте AbstractNotificationSender с общей проверкой адресата и абстрактным doSend.
 * Реализуйте email и console варианты. Объясните, почему публичный алгоритм можно
 * сделать final и где остаётся точка расширения.
 */
public class Task04AbstractBaseClass {

    static abstract class AbstractNotificationSender implements NotificationSender {
        protected final String channelName;

        protected AbstractNotificationSender(String channelName) {
            this.channelName = channelName;
        }

        /**
         * Шаблонный метод (Template Method). Объявляется final, чтобы ни один подкласс
         * не мог обойти обязательную валидацию адресата.
         */
        @Override
        public final void send(Notification notification) {
            validateRecipient(notification);
            doSend(notification);
        }

        private void validateRecipient(Notification notification) {
            if (notification == null) {
                throw new IllegalArgumentException("Уведомление не может быть null");
            }
            if (notification.getRecipient() == null || notification.getRecipient().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "[" + channelName + "] Ошибка: пустой адресат для темы '" + notification.getSubject() + "'");
            }
        }

        /**
         * Защищённая точка расширения для конкретных способов отправки.
         */
        protected abstract void doSend(Notification notification);
    }

    static class EmailNotificationSender extends AbstractNotificationSender {
        private final String smtpServer;

        EmailNotificationSender(String smtpServer) {
            super("EMAIL");
            this.smtpServer = smtpServer;
        }

        @Override
        protected void doSend(Notification n) {
            System.out.println("  [EMAIL via " + smtpServer + "] Кому: " + n.getRecipient()
                    + " | Тема: " + n.getSubject() + " | Текст: " + n.getBody());
        }
    }

    static class ConsoleNotificationSender extends AbstractNotificationSender {
        ConsoleNotificationSender() {
            super("CONSOLE");
        }

        @Override
        protected void doSend(Notification n) {
            System.out.println("  [CONSOLE] Печать для " + n.getRecipient() + ": " + n.getBody());
        }
    }

    public static void main(String[] args) {
        AbstractNotificationSender email = new EmailNotificationSender("smtp.sibguti.ru");
        AbstractNotificationSender console = new ConsoleNotificationSender();

        Notification valid = new Notification("ivanov@sibguti.ru", "ЛР4", "Задания проверены");
        Notification invalid = new Notification("   ", "ЛР4", "Пустой получатель");

        System.out.println("=== 1. Отправка корректного уведомления ===");
        email.send(valid);
        console.send(valid);

        System.out.println("\n=== 2. Срабатывание общей проверки адресата в базовом классе ===");
        try {
            console.send(invalid);
        } catch (IllegalArgumentException e) {
            System.out.println("  Перехвачено исключение: " + e.getMessage());
        }

        System.out.println("\n=== 3. Объяснение ===");
        System.out.println("  - Почему публичный метод send() сделан final:");
        System.out.println("    Он фиксирует инвариант алгоритма (сначала проверка адресата, затем отправка).");
        System.out.println("    Наследники физически не могут переопределить send() и забыть вызвать валидацию.");
        System.out.println("  - Где остаётся точка расширения:");
        System.out.println("    В методе protected abstract void doSend(Notification), который реализуют подклассы.");
    }
}

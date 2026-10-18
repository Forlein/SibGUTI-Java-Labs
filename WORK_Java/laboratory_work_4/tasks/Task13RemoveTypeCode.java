package tasks;

import common.Notification;
import java.util.List;

/**
 * Задание 13. Удаление type code.
 * Дан класс с String notificationType и большим switch.
 * Переведите различия поведения в реализации интерфейса.
 * Не создавайте класс на каждую строку конфигурации: объясните границу оправданного полиморфизма.
 */
public class Task13RemoveTypeCode {

    // === 1. До рефакторинга: класс с String notificationType и switch ===
    static class LegacyTypeCodeSender {
        private final String notificationType;

        LegacyTypeCodeSender(String notificationType) {
            this.notificationType = notificationType;
        }

        public void send(Notification n) {
            switch (notificationType) {
                case "email" -> System.out.println("  [Legacy email] " + n.getBody() + " -> " + n.getRecipient());
                case "sms" -> System.out.println("  [Legacy sms]   " + n.getBody().substring(0, Math.min(20, n.getBody().length())));
                case "push" -> System.out.println("  [Legacy push]  Push-токены для " + n.getSubject());
                default -> throw new IllegalArgumentException("Неизвестный тип: " + notificationType);
            }
        }
    }

    // === 2. После рефакторинга: интерфейс NotificationChannel и параметризуемые реализации ===
    interface NotificationChannel {
        String channelCode();
        void deliver(Notification notification);
    }

    static class EmailChannel implements NotificationChannel {
        private final String smtpHost;
        private final int port;

        EmailChannel(String smtpHost, int port) {
            this.smtpHost = smtpHost;
            this.port = port;
        }

        @Override
        public String channelCode() {
            return "email(" + smtpHost + ":" + port + ")";
        }

        @Override
        public void deliver(Notification n) {
            System.out.println("  [EmailChannel " + smtpHost + ":" + port + "] -> " + n.getRecipient() + ": " + n.getBody());
        }
    }

    static class SmsChannel implements NotificationChannel {
        private final int maxChars;

        SmsChannel(int maxChars) {
            this.maxChars = maxChars;
        }

        @Override
        public String channelCode() {
            return "sms(max=" + maxChars + ")";
        }

        @Override
        public void deliver(Notification n) {
            String text = n.getBody();
            if (text.length() > maxChars) {
                text = text.substring(0, maxChars - 3) + "...";
            }
            System.out.println("  [SmsChannel max=" + maxChars + "] -> " + n.getRecipient() + ": " + text);
        }
    }

    static class WebhookChannel implements NotificationChannel {
        private final String endpointUrl;

        WebhookChannel(String endpointUrl) {
            this.endpointUrl = endpointUrl;
        }

        @Override
        public String channelCode() {
            return "webhook(" + endpointUrl + ")";
        }

        @Override
        public void deliver(Notification n) {
            System.out.println("  [WebhookChannel POST " + endpointUrl + "] payload=\"" + n.getSubject() + "\"");
        }
    }

    public static void main(String[] args) {
        Notification note = new Notification("student@sibguti.ru", "Расписание", "Экзамен перенесен на 10:00 в ауд. 305");

        System.out.println("=== 1. До удаления type code ===");
        new LegacyTypeCodeSender("email").send(note);

        System.out.println("\n=== 2. После замены type code на полиморфные реализации NotificationChannel ===");
        // Два разных SMTP-сервера и два разных лимита SMS обслуживаются одними и теми же классами с разными параметрами!
        List<NotificationChannel> channels = List.of(
                new EmailChannel("smtp.sibguti.ru", 587),
                new EmailChannel("backup-mail.sibguti.ru", 465),
                new SmsChannel(25),
                new SmsChannel(160),
                new WebhookChannel("https://api.sibguti.ru/notify")
        );

        for (NotificationChannel channel : channels) {
            channel.deliver(note);
        }

        System.out.println("\n=== 3. Граница оправданного полиморфизма ===");
        System.out.println("  - Новый КЛАСС создаётся только тогда, когда меняется АЛГОРИТМ / ПОВЕДЕНИЕ (SMTP vs SMS-обрезка vs HTTP POST).");
        System.out.println("  - Параметры КОНФИГУРАЦИИ (адрес хоста, порт, лимит длины SMS, URL вебхука) передаются как поля в конструктор.");
        System.out.println("  - Создавать классы вроде EmailSmtp587Channel или Sms160Channel — антипаттерн, раздувающий иерархию.");
    }
}

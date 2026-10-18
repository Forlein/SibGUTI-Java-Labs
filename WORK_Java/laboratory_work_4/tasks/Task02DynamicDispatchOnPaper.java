package tasks;

import common.Notification;
import common.NotificationSender;

/**
 * Задание 2. Dynamic dispatch на бумаге.
 * Для иерархии NotificationSender -> EmailSender, ConsoleSender предскажите результат
 * вызовов через ссылки разных compile-time типов. Добавьте перегруженный метод
 * и отделите overload resolution от override dispatch.
 */
public class Task02DynamicDispatchOnPaper {

    enum Channel {
        EMAIL, CONSOLE, SMS
    }

    interface ExtendedNotificationSender extends NotificationSender {
        @Override
        void send(Notification notification);

        // Перегруженные (overloaded) методы с другими списками параметров
        void send(Notification notification, Channel channel);

        void send(Channel channel, Notification notification);
    }

    static class EmailSender implements ExtendedNotificationSender {
        private final String smtpHost;

        EmailSender(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        @Override
        public void send(Notification n) {
            System.out.println("  [EmailSender.send(Notification)] SMTP=" + smtpHost + " -> " + n);
        }

        @Override
        public void send(Notification n, Channel channel) {
            System.out.println("  [EmailSender.send(Notification, Channel)] канал=" + channel);
            if (channel == Channel.EMAIL) {
                send(n);
            }
        }

        @Override
        public void send(Channel channel, Notification n) {
            System.out.println("  [EmailSender.send(Channel, Notification)] channel-first вызов");
            send(n);
        }
    }

    static class ConsoleSender implements ExtendedNotificationSender {
        @Override
        public void send(Notification n) {
            System.out.println("  [ConsoleSender.send(Notification)] печать в консоль: " + n);
        }

        @Override
        public void send(Notification n, Channel channel) {
            System.out.println("  [ConsoleSender.send(Notification, Channel)] канал=" + channel);
            if (channel == Channel.CONSOLE) {
                send(n);
            }
        }

        @Override
        public void send(Channel channel, Notification n) {
            System.out.println("  [ConsoleSender.send(Channel, Notification)] channel-first вызов");
            send(n);
        }
    }

    // Демонстрация overload resolution по статическому типу параметра:
    static void route(NotificationSender sender, Notification n) {
        System.out.println("  Вызван route(NotificationSender) [выбрано компилятором по типу ссылки]");
        sender.send(n); // а здесь внутри сработает override dispatch в runtime!
    }

    static void route(EmailSender sender, Notification n) {
        System.out.println("  Вызван route(EmailSender) [выбрано компилятором по типу ссылки]");
        sender.send(n);
    }

    public static void main(String[] args) {
        Notification note = new Notification("student@sibguti.ru", "Зачет", "Ваш зачет проставлен");

        ExtendedNotificationSender polyEmail = new EmailSender("smtp.sibguti.ru");
        ExtendedNotificationSender polyConsole = new ConsoleSender();
        EmailSender concreteEmail = new EmailSender("mail.sibguti.ru");

        System.out.println("=== 1. Вызовы через ссылки разных compile-time типов ===");
        polyEmail.send(note);      // Предсказание: EmailSender.send(Notification)
        polyConsole.send(note);    // Предсказание: ConsoleSender.send(Notification)
        concreteEmail.send(note);  // Предсказание: EmailSender.send(Notification)

        System.out.println("\n=== 2. Перегруженные методы внутри интерфейса (Overload + Override) ===");
        polyEmail.send(note, Channel.EMAIL);   // Компилятор выбирает send(Notification, Channel), JVM — EmailSender
        polyEmail.send(Channel.EMAIL, note);   // Компилятор выбирает send(Channel, Notification), JVM — EmailSender

        System.out.println("\n=== 3. Отделение Overload Resolution (compile-time) от Override Dispatch (runtime) ===");
        System.out.println("Передаем polyEmail (compile-time тип: ExtendedNotificationSender, runtime тип: EmailSender):");
        route(polyEmail, note);
        System.out.println("Передаем concreteEmail (compile-time тип: EmailSender, runtime тип: EmailSender):");
        route(concreteEmail, note);

        System.out.println("\nВывод:");
        System.out.println("  - Overload resolution выполняется КОМПИЛЯТОРОМ по compile-time типам аргументов.");
        System.out.println("  - Override dispatch выполняется JVM ВО ВРЕМЯ ВЫПОЛНЕНИЯ по фактическому классу объекта.");
    }
}

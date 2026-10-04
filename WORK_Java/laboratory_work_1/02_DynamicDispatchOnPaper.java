/**
 * Задание 2. «Dynamic dispatch на бумаге».
 *
 * Иерархия NotificationSender -> EmailSender, ConsoleSender показывает,
 * что при одинаковом статическом типе ссылки результат вызова определяется
 * фактическим классом объекта. Одновременно показано, что перегрузка
 * (overload) выбирается компилятором по типам аргументов, а переопределение
 * (override) — JVM по типу объекта, и что статические методы интерфейса
 * не наследуются классами-реализациями, а скрываются.
 *
 * Запуск: javac 02_DynamicDispatchOnPaper.java
 *         java  -cp . DynamicDispatchOnPaper
 */
class DynamicDispatchOnPaper {

    /**
     * Данные письма: адресат, тема и текст. Класс-структура без поведения;
     * все три геттера нужны отправителям для формирования вывода.
     */
    static class Notification {

        private final String recipient;
        private final String subject;
        private final String body;

        /**
         * Создаёт уведомление. Вызывается из main для демонстрации
         * всех сценариев отправки (поддерживаемый и неподдерживаемый канал).
         */
        Notification(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }

        /**
         * Возвращает адресата. Используется всеми реализациями
         * NotificationSender при печати результата отправки.
         */
        String getRecipient() {
            return recipient;
        }

        /**
         * Возвращает тему письма. Используется EmailSender и ConsoleSender
         * в своих переопределениях send(Notification).
         */
        String getSubject() {
            return subject;
        }

        /**
         * Возвращает текст письма. Зарезервирован для будущих реализаций:
         * текущие отправители печатают тему и адресата.
         */
        String getBody() {
            return body;
        }

        /**
         * Переопределяет Object.toString() для компактного вывода
         * «получатель, тема». Используется при отладке в main.
         */
        @Override
        public String toString() {
            return "Notification[" + recipient + ", " + subject + "]";
        }
    }

    /**
     * Перечисление каналов доставки. Существует, чтобы перегруженные
     * методы send различались ТИПОМ аргумента, а не числом аргументов.
     */
    enum Channel {

        /** Письмо по электронной почте — поддерживается EmailSender. */
        EMAIL,

        /** Печать в консоль — поддерживается ConsoleSender. */
        CONSOLE,

        /** SMS — не поддерживается ни одной из реализаций. */
        SMS
    }

    /**
     * Базовый контракт отправителя. Выбран интерфейс, потому что
     * отправителей может быть сколько угодно, общего состояния у них нет,
     * а иерархия растёт вширь.
     */
    interface NotificationSender {

        /**
         * Основной метод контракта — «отправить уведомление».
         * Реализации обязаны дать своё тело; при вызове через ссылку
         * NotificationSender JVM выбирает версию по фактическому типу.
         */
        void send(Notification n);

        /**
         * Перегруженный метод: отправить в указанный канал.
         * Компилятор выбирает его по числу и типам аргументов на этапе
         * компиляции, а уже внутри тела вызывается переопределённый
         * send(n) — то есть работает динамическая диспетчеризация.
         */
        void send(Notification n, Channel channel);

        /**
         * Перегруженный метод с обратным порядком аргументов.
         * Отличается от send(Notification, Channel) только типами первого
         * параметра, поэтому компилятор трактует его как отдельный метод —
         * на этом строится эксперимент по overload resolution.
         */
        void send(Channel channel, Notification n);

        /**
         * Статический метод интерфейса: служебный, не привязанный к экземпляру.
         * Переопределить его нельзя, и классы-реализации его НЕ наследуют —
         * они могут лишь объявить метод с тем же именем, который скроет
         * интерфейсный. Оба варианта вызываются из main в шаге 7.
         */
        static void describeCapability() {
            System.out.println("      [NotificationSender.describeCapability] вызвана версия ИНТЕРФЕЙСА");
        }
    }

    /**
     * Реализация №1: отправка по SMTP. Создаётся в main напрямую
     * и входит в полиморфный массив senders.
     */
    static class EmailSender implements NotificationSender {

        private final String smtpHost;

        /**
         * Задаёт SMTP-хост — это состояние реализации, которого нет
         * и не может быть у интерфейса. Вызывается из main.
         */
        EmailSender(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        /**
         * Переопределение основного метода контракта: печатает SMTP-хост,
         * адресата и тему. Вызывается полиморфно из main и рекурсивно
         * из перегруженных send(...) этого же класса.
         */
        @Override
        public void send(Notification n) {
            System.out.println("      [EmailSender] SMTP=" + smtpHost
                    + " -> " + n.getRecipient() + ": " + n.getSubject());
        }

        /**
         * Переопределение перегруженного метода: при канале EMAIL
         * делегирует собственному send(n), иначе сообщает о неподдержке.
         * Вызывается из main для проверки сочетания overload и override.
         */
        @Override
        public void send(Notification n, Channel channel) {
            if (channel == Channel.EMAIL) {
                send(n);
            } else {
                System.out.println("      [EmailSender] канал " + channel
                        + " не поддерживается, отправка пропущена");
            }
        }

        /**
         * Переопределение варианта с channel-first: печатает маркер
         * защищённого входа и передаёт управление в send(n).
         * Вызывается из main, чтобы показать, какой именно метод
         * выбрал компилятор по типам аргументов.
         */
        @Override
        public void send(Channel channel, Notification n) {
            System.out.println("      [EmailSender] защищённый вход (channel-first)");
            send(n);
        }

        /**
         * Статический метод с тем же именем, что и в интерфейсе.
         * Он не переопределяет интерфейсный, а скрывает его: вызов
         * EmailSender.describeCapability() из main выполнит именно
         * эту версию. Проверяется в шаге 7.
         */
        static void describeCapability() {
            System.out.println("      [EmailSender.describeCapability] вызвана версия КЛАССА"
                    + " (она «скрыла» версию интерфейса)");
        }
    }

    /**
     * Реализация №2: печать в консоль. Ни строчки общего кода с EmailSender —
     * оба класса независимы и лишь подчиняются общему контракту.
     */
    static class ConsoleSender implements NotificationSender {

        /**
         * Переопределение основного метода контракта: печатает тему
         * и адресата в консоль. Вызывается полиморфно из main.
         */
        @Override
        public void send(Notification n) {
            System.out.println("      [ConsoleSender] печать: \"" + n.getSubject()
                    + "\" для " + n.getRecipient());
        }

        /**
         * Переопределение перегруженного метода: при канале CONSOLE
         * делегирует send(n), иначе отказывает. Вызывается из main
         * для проверки независимой проверки канала в каждой реализации.
         */
        @Override
        public void send(Notification n, Channel channel) {
            if (channel == Channel.CONSOLE) {
                send(n);
            } else {
                System.out.println("      [ConsoleSender] канал " + channel
                        + " не поддерживается, отправка пропущена");
            }
        }

        /**
         * Переопределение варианта с channel-first: печатает маркер
         * защищённого входа и вызывает send(n). Вызывается из main.
         * Своего статического describeCapability() класс не объявляет,
         * поэтому статические методы интерфейса ему не достаются.
         */
        @Override
        public void send(Channel channel, Notification n) {
            System.out.println("      [ConsoleSender] защищённый вход (channel-first)");
            send(n);
        }
    }

    /**
     * Точка входа. Последовательно показывает: вызовы через ссылки
     * интерфейсного типа, через ссылки конкретных типов, полиморфный массив
     * смешанных реализаций, overload resolution на этапе компиляции,
     * недоступность send() у ссылки типа Object и скрытие статических
     * методов интерфейса.
     */
    public static void main(String[] args) {

        Notification n = new Notification("student@sibguti.ru", "Оплата", "Счёт на 5000 руб.");

        System.out.println("=== 1. Через ссылку ИНТЕРФЕЙСНОГО типа ===");
        NotificationSender s1 = new EmailSender("smtp.sibguti.ru");
        NotificationSender s2 = new ConsoleSender();
        s1.send(n);
        s2.send(n);

        System.out.println();
        System.out.println("=== 2. Через ссылку КОНКРЕТНОГО типа ===");
        EmailSender concreteEmail = new EmailSender("smtp.sibguti.ru");
        ConsoleSender concreteConsole = new ConsoleSender();
        concreteEmail.send(n);
        concreteConsole.send(n);

        System.out.println();
        System.out.println("=== 3. Полиморфный массив: смешанные ссылки ===");
        NotificationSender[] senders = {
                new EmailSender("smtp.sibguti.ru"),
                new EmailSender("mail.sibguti.ru"),
                new ConsoleSender()
        };
        for (NotificationSender s : senders) {
            s.send(n);
        }

        System.out.println();
        System.out.println("=== 4. OVERLOAD RESOLUTION (выбор на КОМПИЛЯЦИИ) ===");
        System.out.println("Вызов s1.send(n, Channel.EMAIL):");
        s1.send(n, Channel.EMAIL);
        System.out.println("Вызов s1.send(Channel.EMAIL, n):");
        s1.send(Channel.EMAIL, n);
        System.out.println("Оба вызова «на бумаге» отличаются только порядком аргументов,");
        System.out.println("но это РАЗНЫЕ методы — компилятор выбрал их по типам аргументов.");

        System.out.println();
        System.out.println("=== 5. OVERLOAD + OVERRIDE: две оси сразу ===");
        System.out.println("s2.send(n, Channel.CONSOLE)  (ConsoleSender, поддерживает):");
        s2.send(n, Channel.CONSOLE);
        System.out.println("s2.send(n, Channel.EMAIL)     (ConsoleSender, НЕ поддерживает):");
        s2.send(n, Channel.EMAIL);
        System.out.println("s1.send(n, Channel.SMS)       (EmailSender, НЕ поддерживает):");
        s1.send(n, Channel.SMS);

        System.out.println();
        System.out.println("=== 6. ЧТО КОМПИЛЯТОР ЗАПРЕЩАЕТ (доказательство границы) ===");
        System.out.println("Object o = s1;            // повышаем тип до Object");
        Object o = s1;
        System.out.println("Object.class.getSimpleName() = " + o.getClass().getSimpleName()
                + "  <- фактический тип всё ещё виден в рантайме");
        NotificationSender back = (NotificationSender) o;
        System.out.println("после приведения вниз: " + back.getClass().getSimpleName());

        System.out.println();
        System.out.println("=== 7. СТАТИЧЕСКИЕ МЕТОДЫ — ещё один пример compile-time выбора ===");
        NotificationSender.describeCapability();
        EmailSender.describeCapability();
        System.out.println("   (ConsoleSender.describeCapability() не скомпилировался бы:");
        System.out.println("    error: cannot find symbol — статические методы интерфейса");
        System.out.println("    классам-реализациям не наследуются, в отличие от обычных)");
    }
}

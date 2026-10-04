import java.util.ArrayList;
import java.util.List;

/**
 * Задание 4. «Абстрактный базовый класс».
 *
 * Показывает шаблонный метод: публичный final-алгоритм send(...) в
 * AbstractNotificationSender задаёт порядок шагов (валидация, хуки,
 * главное действие, журнал), а наследники реализуют только абстрактный
 * doSend(...) и при желании переопределяют хуки. Общий код не дублируется,
 * инварианты защищены на этапе компиляции, а новая точка расширения —
 * один класс с одним методом.
 *
 * Запуск: javac 04_AbstractBaseClass.java
 *         java  -cp . AbstractBaseClass
 */
class AbstractBaseClass {

    /**
     * Данные уведомления: адресат, тема и текст. Валидирующая проверка
     * в базовом классе вызывает getRecipient() и getSubject(), а реализации
     * doSend(...) — getBody().
     */
    static class Notification {

        private final String recipient;
        private final String subject;
        private final String body;

        /**
         * Создаёт уведомление. Вызывается из main для проверки успешной
         * отправки и для проверки срабатывания общей валидации.
         */
        Notification(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }

        /**
         * Возвращает адресата. Используется методом validate(...) базового
         * класса для проверки непустоты и всеми реализациями doSend(...)
         * для печати результата.
         */
        String getRecipient() {
            return recipient;
        }

        /**
         * Возвращает тему. Используется в validate(...), в хуках
         * beforeSend/afterSend, в журнале send(...) и в toString().
         */
        String getSubject() {
            return subject;
        }

        /**
         * Возвращает текст. Используется реализациями doSend(...) —
         * EmailSender печатает его целиком, SmsSender обрезает по maxLength.
         */
        String getBody() {
            return body;
        }

        /**
         * Переопределяет Object.toString() краткой формой «тема -> адресат».
         * Используется в журнале send(...) для читаемых записей.
         */
        @Override
        public String toString() {
            return "\"" + subject + "\" -> " + recipient;
        }
    }

    /**
     * Абстрактный базовый класс отправителя: содержит общий алгоритм
     * send(...), общую приватную валидацию и точку расширения doSend(...).
     * Создать экземпляр напрямую нельзя — только наследника.
     */
    static abstract class AbstractNotificationSender {

        protected final String channelName;
        private final List<String> journal = new ArrayList<String>();

        /**
         * Конструктор базового класса: сохраняет имя канала для журналов.
         * Обязателен для каждого наследника (вызывается через super(...) —
         * неявно или явно) и защищён от вызова клиентом.
         */
        protected AbstractNotificationSender(String channelName) {
            this.channelName = channelName;
        }

        /**
         * Шаблонный метод: публичный алгоритм отправки, который нельзя
         * переопределить (final). Выполняет validate(...), beforeSend(...),
         * doSend(...) — единственный шаг, уходящий в наследника, —
         * afterSend(...) и запись в журнал. Вызывается клиентом sendAll(...)
         * и напрямую из main для проверки валидации и журнала.
         */
        public final void send(Notification n) {
            validate(n);
            beforeSend(n);
            doSend(n);
            afterSend(n);
            journal.add("OK   " + n.getSubject() + " via " + channelName);
        }

        /**
         * Абстрактная точка расширения: «как именно отправить».
         * Обязательна для всех наследников (EmailSender, ConsoleSender,
         * SmsSender). Вызывается только из send(...), поэтому клиент
         * не может вызвать её в обход валидации и журнала.
         */
        protected abstract void doSend(Notification n);

        /**
         * Хук «до отправки» с пустым телом по умолчанию.
         * Вызывается из send(...) перед doSend(...); переопределяется
         * только EmailSender, чтобы вывести подготовку письма.
         */
        protected void beforeSend(Notification n) {
        }

        /**
         * Хук «после отправки» с пустым телом по умолчанию.
         * Вызывается из send(...) после doSend(...) перед записью
         * в журнал; в задании не переопределяется, но служит местом
         * для метрик.
         */
        protected void afterSend(Notification n) {
        }

        /**
         * Общая приватная проверка: запрещает null-уведомление и пустого
         * адресата, а удачную проверку записывает в журнал. Вызывается
         * первым шагом из send(...). Private означает, что ни клиент,
         * ни наследник не могут её отключить или подменить.
         */
        private void validate(Notification n) {
            if (n == null) {
                throw new IllegalArgumentException("уведомление не должно быть null");
            }
            if (n.getRecipient() == null || n.getRecipient().trim().isEmpty()) {
                throw new IllegalArgumentException("пустой адресат у: " + n.getSubject());
            }
            journal.add("CHECK " + n.getSubject() + " (адресат ок)");
        }

        /**
         * Возвращает копию журнала отправок. Вызывается из main при
         * демонстрации единообразного журнала. Копия, а не сам список,
         * чтобы клиент не мог очистить внутреннее состояние.
         */
        public final List<String> getJournal() {
            return new ArrayList<String>(journal);
        }

        /**
         * Возвращает имя канала. Используется внутри send(...) при записи
         * в журнал и доступно клиенту как final-геттер, поскольку значение
         * задаётся только в конструкторе и не меняется.
         */
        public final String getChannelName() {
            return channelName;
        }
    }

    /**
     * Наследник №1: отправка по SMTP. Наследует весь алгоритм send(...)
     * и реализует только doSend(...) плюс хук beforeSend(...).
     */
    static class EmailSender extends AbstractNotificationSender {

        private final String smtpHost;

        /**
         * Конструктор наследника: первым оператором вызывает super("EMAIL")
         * для инициализации базового состояния, затем задаёт свой SMTP-хост.
         * Вызывается из main при сборке массива отправителей.
         */
        EmailSender(String smtpHost) {
            super("EMAIL");
            this.smtpHost = smtpHost;
        }

        /**
         * Реализация точки расширения: печатает текст письма и адресата
         * вместе с SMTP-хостом. Вызывается диспетчеризованно из send(...).
         */
        @Override
        protected void doSend(Notification n) {
            System.out.println("   [EMAIL " + smtpHost + "] отправляю " + n.getBody()
                    + " на " + n.getRecipient());
        }

        /**
         * Переопределение хука «до отправки»: выводит тему письма
         * как подготовку к отправке. Вызывается из send(...) до doSend(...).
         */
        @Override
        protected void beforeSend(Notification n) {
            System.out.println("   [EMAIL] подготовка письма: тема=\"" + n.getSubject() + "\"");
        }
    }

    /**
     * Наследник №2: печать в консоль. Ни строчки общего кода с EmailSender,
     * но алгоритм send(...) и валидация — те же, из базового класса.
     */
    static class ConsoleSender extends AbstractNotificationSender {

        /**
         * Конструктор наследника: передаёт базовому классу имя канала "CONSOLE".
         * Вызывается из main — в массив отправителей, в проверку валидации
         * и в проверку журнала.
         */
        ConsoleSender() {
            super("CONSOLE");
        }

        /**
         * Реализация точки расширения: печатает текст и адресата в консоль.
         * Вызывается диспетчеризованно из send(...).
         */
        @Override
        protected void doSend(Notification n) {
            System.out.println("   [CONSOLE] печатаю: " + n.getBody()
                    + " (кому: " + n.getRecipient() + ")");
        }
    }

    /**
     * Наследник №3: SMS с собственным правилом длины. Добавлен без
     * изменения базового класса — стоимость нового канала равна одному
     * классу с одним методом.
     */
    static class SmsSender extends AbstractNotificationSender {

        private final int maxLength;

        /**
         * Конструктор наследника: задаёт имя канала "SMS" и лимит длины
         * сообщения. Вызывается из main при сборке массива отправителей.
         */
        SmsSender(int maxLength) {
            super("SMS");
            this.maxLength = maxLength;
        }

        /**
         * Реализация точки расширения: обрезает текст до maxLength с
         * многоточием и печатает результат. Правило остаётся здесь,
         * а не в базовом классе, потому что касается только SMS.
         * Вызывается диспетчеризованно из send(...).
         */
        @Override
        protected void doSend(Notification n) {
            String text = n.getBody();
            if (text.length() > maxLength) {
                text = text.substring(0, maxLength) + "...";
            }
            System.out.println("   [SMS] отправляю " + text + " на " + n.getRecipient());
        }
    }

    /**
     * Клиент: отправляет одно уведомление всем отправителям массива.
     * Принимает базовый тип, поэтому появление нового канала
     * (например, TelegramSender) не потребует правок. Вызывается из main
     * для демонстрации работы всех трёх реализаций.
     */
    static void sendAll(AbstractNotificationSender[] senders, Notification n) {
        for (AbstractNotificationSender sender : senders) {
            sender.send(n);
        }
    }

    /**
     * Точка входа: прогоняет отправку через sendAll(...), показывает, что
     * общая валидация срабатывает независимо от канала, печатает журнал
     * и объясняет цену final для send(...) и список оставшихся точек
     * расширения. Использует sendAll, getJournal и validate косвенно.
     */
    public static void main(String[] args) {

        System.out.println("=== 1. Работа через базовый тип (final-алгоритм) ===");
        AbstractNotificationSender[] senders = {
                new EmailSender("smtp.sibguti.ru"),
                new ConsoleSender(),
                new SmsSender(20)
        };
        sendAll(senders, new Notification("ivanov@sibguti.ru", "Дедлайн", "Сдайте лабу до пятницы"));

        System.out.println();
        System.out.println("=== 2. Общая валидация срабатывает ДЛЯ ВСЕХ ===");
        try {
            AbstractNotificationSender any = new ConsoleSender();
            any.send(new Notification("   ", "Пустой адресат", "тест"));
        } catch (IllegalArgumentException e) {
            System.out.println("   поймали: " + e.getMessage());
            System.out.println("   (EmailSender сломался бы точно так же — код проверки один)");
        }

        System.out.println();
        System.out.println("=== 3. Журнал формируется единообразно ===");
        AbstractNotificationSender console = new ConsoleSender();
        console.send(new Notification("petrov@sibguti.ru", "Оценки", "Итог: 85"));
        for (String line : console.getJournal()) {
            System.out.println("   " + line);
        }

        System.out.println();
        System.out.println("=== 4. ПОЧЕМУ send(...) ОБЪЯВЛЕН final ===");
        System.out.println("   а) валидация и журнал обязаны выполняться ВСЕГДА;");
        System.out.println("   б) клиент вызывает один и тот же публичный метод;");
        System.out.println("   в) doSend() protected — в обход вызвать нельзя;");
        System.out.println("   г) новый канал = новый класс, а не правка алгоритма.");
        System.out.println("   Попытка переопределить send() дала бы ошибку компиляции:");
        System.out.println("   'error: cannot override final method send(Notification)' —");
        System.out.println("   то есть «забыть» валидацию физически невозможно.");

        System.out.println();
        System.out.println("=== 5. ГДЕ ИМЕННО ОСТАЁТСЯ ТОЧКА РАСШИРЕНИЯ ===");
        System.out.println("   1) protected abstract doSend(...)  — ОБЯЗАТЕЛЬНО переопределить;");
        System.out.println("   2) protected beforeSend(...)      — можно расширить шаги;");
        System.out.println("   3) protected afterSend(...)       — можно расширить шаги;");
        System.out.println("   4) конструктор суперкласса        — можно задать своё состояние;");
        System.out.println("   Всё остальное — final или private и закрыто намертво.");

        System.out.println();
        System.out.println("=== 6. ЦЕНА ВЫБОРА: abstract class vs interface ===");
        System.out.println("   + общий код в одном месте (нет копипасты проверки/журнала);");
        System.out.println("   + можно protected-состояние и хуки;");
        System.out.println("   - класс НАСЛЕДУЕТСЯ ТОЛЬКО ОДИН (нет множественного);");
        System.out.println("   - клиент привязан к иерархии, а не к контракту.");
        System.out.println("   Подробные сравнения — в задании 5.");
    }
}

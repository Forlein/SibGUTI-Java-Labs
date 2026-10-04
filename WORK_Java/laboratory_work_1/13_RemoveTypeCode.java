import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Задание 13. «Удаление type code».
 *
 * Задача файла: взять класс с полем-строкой notificationType и большим
 * switch, перевести различия поведения в реализации интерфейса
 * и обосновать границу оправданного полиморфизма, чтобы не заводить класс
 * на каждую строку конфигурации.
 *
 * «Болезнь type code»: тип вынесен в данные, а не в типы. Следствия —
 * выбор поведения по строке в рантайме, опечатка как баг, который не ловит
 * компилятор, повтор одного и того же сравнения строк в разных методах
 * и необходимость править диспетчер в нескольких местах при добавлении канала.
 *
 * Запуск: javac 13_RemoveTypeCode.java
 *         java -cp . RemoveTypeCode
 *
 * Требования к среде: JDK 8+, где появились Optional и полиморфизм,
 * на котором построено решение.
 */
class RemoveTypeCode {

    /**
     * Домен: неизменяемое уведомление, одинаковое для всех каналов доставки.
     * Содержит адресата (почта, телефон, токен), тему и текст.
     */
    static class Notification {

        private final String recipient;
        private final String subject;
        private final String body;

        /**
         * Сохраняет адресата, тему и текст уведомления. Вызывается из main
         * при создании единственного уведомления для всех экспериментов.
         */
        Notification(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }

        /**
         * Возвращает адресата: e-mail, номер телефона или токен устройства.
         * Используется всеми реализациями NotificationChannel в deliver(...).
         */
        String getRecipient() {
            return recipient;
        }

        /**
         * Возвращает тему уведомления. Используется PushChannel в deliver(...),
         * диспетчером в getLog-подобном журнале и toString().
         */
        String getSubject() {
            return subject;
        }

        /**
         * Возвращает текст уведомления. Используется EmailChannel, SmsChannel
         * и WebhookChannel в deliver(...).
         */
        String getBody() {
            return body;
        }

        /**
         * Возвращает представление «тема -> адресат». Вызывается в
         * sendConsole(...) плохого диспетчера и в ConsoleChannel.deliver(...).
         */
        @Override
        public String toString() {
            return subject + " -> " + recipient;
        }
    }

    /**
     * Вариант «до»: типичный, но плохой диспетчер.
     *
     * Оставлен рабочим намеренно, чтобы проблему можно было увидеть
     * в действии: тип уведомления хранится в строке, поведение выбирается
     * сравнением строк, а набор каналов продублирован в двух методах.
     */
    static class NotificationDispatcherBad {

        private final List<Notification> log = new ArrayList<Notification>();

        /**
         * Отправляет уведомление, выбирая способ по строке-типу. Забытая
         * ветка обнаруживается только в рантайме. Вызывается из main для
         * четырёх корректных типов и для опечатки "emial".
         */
        void dispatch(Notification n, String notificationType) {
            if ("email".equals(notificationType)) {
                sendEmail(n);
            } else if ("sms".equals(notificationType)) {
                sendSms(n);
            } else if ("push".equals(notificationType)) {
                sendPush(n);
            } else if ("console".equals(notificationType)) {
                sendConsole(n);
            } else {
                throw new IllegalArgumentException("неизвестный тип уведомления: " + notificationType);
            }
            log.add(n);
        }

        /**
         * Отправляет по почте. Вызывается из dispatch(...) при типе "email".
         */
        private void sendEmail(Notification n) {
            System.out.println("   [email] " + n.getBody() + " на " + n.getRecipient());
        }

        /**
         * Отправляет SMS, обрезая текст до 160 символов. Вызывается из
         * dispatch(...) при типе "sms"; лимит здесь зашит в код метода.
         */
        private void sendSms(Notification n) {
            String text = n.getBody();
            if (text.length() > 160) {
                text = text.substring(0, 157) + "...";
            }
            System.out.println("   [sms]   " + text + " на " + n.getRecipient());
        }

        /**
         * Отправляет push по теме и токену. Вызывается из dispatch(...)
         * при типе "push".
         */
        private void sendPush(Notification n) {
            System.out.println("   [push]  " + n.getSubject() + " на токен " + n.getRecipient());
        }

        /**
         * Печатает уведомление в консоль. Вызывается из dispatch(...)
         * при типе "console".
         */
        private void sendConsole(Notification n) {
            System.out.println("   [console] " + n);
        }

        /**
         * Второе место, где живёт type code: описание способа доставки
         * для документации. Добавление канала требует правки и здесь тоже.
         * Вызывается из main для типа "sms".
         */
        String describeDelivery(String notificationType) {
            if ("email".equals(notificationType)) {
                return "электронная почта (надежная, медленная)";
            } else if ("sms".equals(notificationType)) {
                return "SMS (быстрая, до 160 символов)";
            } else if ("push".equals(notificationType)) {
                return "push-уведомление (нужен токен устройства)";
            } else if ("console".equals(notificationType)) {
                return "консоль (для отладки)";
            }
            return "неизвестный способ: " + notificationType;
        }
    }

    /**
     * Вариант «после»: контракт способа доставки.
     *
     * Ключевая смена мысли: способ доставки становится типом объекта,
     * а не строкой. Строка остаётся только в конфигурации, то есть
     * в реестре каналов.
     */
    interface NotificationChannel {

        /**
         * Возвращает имя канала для логов, журнала и ключа реестра.
         * Вызывается диспетчером, реестром и main при печати отчёта.
         */
        String name();

        /**
         * Выполняет доставку уведомления. Реализация сама решает, как
         * использовать тему, текст и адресата. Вызывается диспетчером
         * NotificationDispatcher.dispatch(...) через динамическую диспетчеризацию.
         */
        void deliver(Notification n);

        /**
         * Возвращает человекочитаемое описание для документации. Вызывается
         * из main при выводе списка зарегистрированных каналов.
         */
        String describe();
    }

    /**
     * Реализация канала электронной почты. Правила доставки почты живут
     * только здесь; SMTP-хост передан как данные, поэтому отдельный класс
     * на каждый хост не заводится.
     */
    static class EmailChannel implements NotificationChannel {

        private final String smtpHost;

        /**
         * Задаёт SMTP-хост как данные. Вызывается из main при сборке
         * реестра каналов.
         */
        EmailChannel(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        /**
         * Возвращает имя "email" — ключ, под которым канал попадает в реестр
         * и под которым печатается в журнале. Вызывается реестром,
         * диспетчером и main.
         */
        @Override
        public String name() {
            return "email";
        }

        /**
         * Печатает доставку по почте с указанием SMTP-хоста. Вызывается
         * из NotificationDispatcher.dispatch(...).
         */
        @Override
        public void deliver(Notification n) {
            System.out.println("   [email] через " + smtpHost + ": " + n.getBody()
                    + " на " + n.getRecipient());
        }

        /**
         * Возвращает описание почтового канала. Вызывается из main
         * при печати отчёта по реестру.
         */
        @Override
        public String describe() {
            return "электронная почта (надежная, медленная)";
        }
    }

    /**
     * Реализация SMS-канала. Свойство канала — лимит длины — стало его
     * поведением и настройкой конструктора, а не условием в диспетчере.
     */
    static class SmsChannel implements NotificationChannel {

        private final int maxLength;

        /**
         * Задаёт лимит длины сообщения как данные. Вызывается из main
         * при сборке реестра каналов.
         */
        SmsChannel(int maxLength) {
            this.maxLength = maxLength;
        }

        /**
         * Возвращает имя "sms" для реестра, журнала и отчёта. Вызывается
         * реестром, диспетчером и main.
         */
        @Override
        public String name() {
            return "sms";
        }

        /**
         * Печатает SMS, предварительно обрезав текст до maxLength и добавив
         * многоточие. Вызывается из NotificationDispatcher.dispatch(...).
         */
        @Override
        public void deliver(Notification n) {
            String text = n.getBody();
            if (text.length() > maxLength) {
                text = text.substring(0, maxLength - 3) + "...";
            }
            System.out.println("   [sms]   " + text + " на " + n.getRecipient());
        }

        /**
         * Возвращает описание SMS-канала с текущим лимитом длины.
         * Вызывается из main при печати отчёта по реестру.
         */
        @Override
        public String describe() {
            return "SMS (быстрая, до " + maxLength + " символов)";
        }
    }

    /**
     * Реализация push-канала. Требование токена устройства проверяется
     * внутри реализации, поэтому клиентский код защищён от неверной
     * конфигурации адресата.
     */
    static class PushChannel implements NotificationChannel {

        private final boolean requireDeviceToken;

        /**
         * Задаёт, требовать ли непустой токен устройства. Вызывается из main
         * при сборке реестра каналов.
         */
        PushChannel(boolean requireDeviceToken) {
            this.requireDeviceToken = requireDeviceToken;
        }

        /**
         * Возвращает имя "push" для реестра, журнала и отчёта. Вызывается
         * реестром, диспетчером и main.
         */
        @Override
        public String name() {
            return "push";
        }

        /**
         * Проверяет наличие токена, если это требуется, и печатает доставку
         * по теме на токен. Вызывается из NotificationDispatcher.dispatch(...).
         */
        @Override
        public void deliver(Notification n) {
            if (requireDeviceToken && n.getRecipient().isEmpty()) {
                throw new IllegalStateException("push без токена устройства невозможен");
            }
            System.out.println("   [push]  " + n.getSubject() + " на токен " + n.getRecipient());
        }

        /**
         * Возвращает описание push-канала. Вызывается из main при печати
         * отчёта по реестру.
         */
        @Override
        public String describe() {
            return "push-уведомление (нужен токен устройства)";
        }
    }

    /**
     * Реализация консольного канала — самая простая: единственное поведение
     * это печать уведомления. Пример того, что не каждая реализация
     * обязана иметь состояние.
     */
    static class ConsoleChannel implements NotificationChannel {

        /**
         * Возвращает имя "console" для реестра, журнала и отчёта. Вызывается
         * реестром, диспетчером и main.
         */
        @Override
        public String name() {
            return "console";
        }

        /**
         * Печатает уведомление через toString(). Вызывается
         * из NotificationDispatcher.dispatch(...).
         */
        @Override
        public void deliver(Notification n) {
            System.out.println("   [console] " + n);
        }

        /**
         * Возвращает описание консольного канала. Вызывается из main
         * при печати отчёта по реестру.
         */
        @Override
        public String describe() {
            return "консоль (для отладки)";
        }
    }

    /**
     * Реализация webhook-канала, добавленная вообще без правок диспетчера —
     * именно это и есть цель задания.
     */
    static class WebhookChannel implements NotificationChannel {

        private final String url;

        /**
         * Задаёт URL внешнего сервиса как данные. Вызывается из main
         * при сборке реестра каналов.
         */
        WebhookChannel(String url) {
            this.url = url;
        }

        /**
         * Возвращает имя "webhook" для реестра, журнала и отчёта. Вызывается
         * реестром, диспетчером и main.
         */
        @Override
        public String name() {
            return "webhook";
        }

        /**
         * Печатает имитацию HTTP-вызова внешнего сервиса с текстом
         * уведомления в теле запроса. Вызывается
         * из NotificationDispatcher.dispatch(...).
         */
        @Override
        public void deliver(Notification n) {
            System.out.println("   [webhook] POST " + url + " с телом \"" + n.getBody() + "\"");
        }

        /**
         * Возвращает описание webhook-канала. Вызывается из main при печати
         * отчёта по реестру.
         */
        @Override
        public String describe() {
            return "HTTP-вызов внешнего сервиса";
        }
    }

    /**
     * Новый диспетчер, который ничего не знает о конкретных типах: в нём
     * нет ни сравнения строк, ни instanceof, ни switch по типу. Он принимает
     * готовый объект-канал и просто вызывает deliver(...).
     */
    static class NotificationDispatcher {

        private final List<String> log = new ArrayList<String>();

        /**
         * Передаёт уведомление готовому каналу и записывает доставку
         * в журнал с именем канала. Вызывается из main в цикле по всем
         * зарегистрированным каналам.
         */
        void dispatch(Notification n, NotificationChannel channel) {
            channel.deliver(n);
            log.add(channel.name() + ": " + n.getSubject());
        }

        /**
         * Возвращает копию журнала доставок. Заготовка для отчётности:
         * вызывается из кода, который печатает историю отправок.
         */
        List<String> getLog() {
            return new ArrayList<String>(log);
        }
    }

    /**
     * Реестр каналов: единственное место в программе, где строки
     * превращаются в объекты. Это граница между внешним миром (конфиги,
     * JSON, переменные окружения, база данных) и системой типов.
     * Наружу отдаётся только интерфейсный тип, поэтому вызывающий код
     * работает с NotificationChannel и не зависит от строк.
     */
    static class ChannelRegistry {

        private final Map<String, NotificationChannel> channels = new HashMap<String, NotificationChannel>();

        /**
         * Регистрирует канал под именем, которое он сам сообщает в name().
         * Настройки остаются аргументами конструктора, а не подклассами.
         * Вызывается из main пять раз при сборке реестра.
         */
        void register(NotificationChannel channel) {
            channels.put(channel.name(), channel);
        }

        /**
         * Ищет канал по имени. Optional выражает «нашлось или нет»
         * без null, поэтому опечатка в конфигурации не приводит
         * к исключению. Вызывается из main для корректного имени и опечатки.
         */
        java.util.Optional<NotificationChannel> byName(String name) {
            return java.util.Optional.ofNullable(channels.get(name));
        }

        /**
         * Возвращает все зарегистрированные каналы для отчёта. Вызывается
         * из main в цикле печати описаний и доставки.
         */
        List<NotificationChannel> all() {
            return new ArrayList<NotificationChannel>(channels.values());
        }
    }

    /**
     * Точка входа: показывает проблему type code на «плохом» диспетчере,
     * собирает реестр из пяти каналов и показывает, что диспетчер не изменился,
     * затем разбирает границу оправданного полиморфизма и сравнивает
     * объём правок при добавлении канала.
     */
    public static void main(String[] args) {

        Notification n = new Notification("student@sibguti.ru", "Дедлайн",
                "Остался один день до сдачи лабораторной работы №4");

        System.out.println("=== 1. «ДО»: строки выбирают поведение ===");
        NotificationDispatcherBad bad = new NotificationDispatcherBad();
        System.out.println("   отправляем как \"email\"");
        bad.dispatch(n, "email");
        System.out.println("   отправляем как \"sms\"");
        bad.dispatch(n, "sms");
        System.out.println("   отправляем как \"push\"");
        bad.dispatch(n, "push");
        System.out.println("   отправляем как \"console\"");
        bad.dispatch(n, "console");

        System.out.println();
        System.out.println("   А теперь опечатка в КОНФИГУРАЦИИ: \"emial\"");
        try {
            bad.dispatch(n, "emial");
        } catch (IllegalArgumentException e) {
            System.out.println("   ... и падает в рантайме: " + e.getMessage());
        }
        System.out.println("   Компилятор не может проверить содержимое строки.");

        System.out.println();
        System.out.println("   Описание способа доставки — тоже по строке:");
        System.out.println("   \"sms\" -> " + bad.describeDelivery("sms"));
        System.out.println("   «type code» повторился в методе describeDelivery:");
        System.out.println("   чтобы добавить webhook, пришлось бы править ДВА метода.");

        System.out.println();
        System.out.println("=== 2. «ПОСЛЕ»: объекты выбирают поведение ===");
        ChannelRegistry registry = new ChannelRegistry();
        registry.register(new EmailChannel("smtp.sibguti.ru"));
        registry.register(new SmsChannel(140));
        registry.register(new PushChannel(true));
        registry.register(new ConsoleChannel());
        registry.register(new WebhookChannel("https://hooks.sibguti.ru/notifications"));

        NotificationDispatcher dispatcher = new NotificationDispatcher();

        for (NotificationChannel channel : registry.all()) {
            System.out.println("   канал " + channel.name() + ": " + channel.describe());
            dispatcher.dispatch(n, channel);
        }

        System.out.println();
        System.out.println("=== 3. ЧТО ПРИНЕСЛА ЗАМЕНА ===");
        System.out.println("   а) Диспетчер не содержит ни if, ни switch, ни instanceof.");
        System.out.println("   б) Опечатка в конфигурации ловится ОДИН раз — при старте:");
        System.out.println("      registry.byName(\"emial\") -> "
                + registry.byName("emial").isPresent() + " (isPresent() = false, а не исключение)");
        System.out.println("   в) Новое поведение добавляется ОДНИМ классом.");
        System.out.println("      WebhookChannel добавлен выше — диспетчер не менялся.");
        System.out.println("   г) Настройки перестали порождать классы:");
        System.out.println("      EmailChannel(\"smtp1\"), EmailChannel(\"smtp2\") — это");
        System.out.println("      ОДИН класс с ДВУМЯ объектами.");

        System.out.println();
        System.out.println("=== 4. ГРАНИЦА ОПРАВДАННОГО ПОЛИМОРФИЗМА ===");
        System.out.println("   ЗАВОДИМ КЛАСС, если канал:");
        System.out.println("     • делает СУЩЕСТВЕННО РАЗНОЕ (алгоритм доставки отличается);");
        System.out.println("     • живёт дольше одной конфигурации (не меняется от настроек);");
        System.out.println("     • его реализации различаются в нескольких методах сразу");
        System.out.println("       (deliver + describe + возможно валидация).");
        System.out.println();
        System.out.println("   НЕ ЗАВОДИМ КЛАСС, если различие — это только ДАННЫЕ:");
        System.out.println("     • другой SMTP-хост;");
        System.out.println("     • другой лимит SMS в символах;");
        System.out.println("     • другой URL вебхука;");
        System.out.println("     • другая тема письма, другой шаблон, флаг «срочно».");
        System.out.println("     Всё это -> конструктор/поля/конфигурационный файл.");
        System.out.println();
        System.out.println("   ПЛОХОЙ ПУТЬ (антипаттерн «тип-в-строке» наоборот):");
        System.out.println("       class EmailChannelSmtp implements NotificationChannel");
        System.out.println("       class EmailChannelSendgrid implements NotificationChannel");
        System.out.println("       class EmailChannelLocalhost implements NotificationChannel");
        System.out.println("       class SmsChannel140  implements NotificationChannel");
        System.out.println("       class SmsChannel160  implements NotificationChannel");
        System.out.println("       class SmsChannelUnicode implements NotificationChannel");
        System.out.println("     Каждая строка конфигурации стала классом. Через полгода");
        System.out.println("     в проекте 60 классов, которые ведут себя одинаково.");
        System.out.println("     Признак: класс отличается от соседнего ТОЛЬКО значением поля.");
        System.out.println();
        System.out.println("   ПРАВИЛО-ЭВРИСТИКА:");
        System.out.println("     «Могу ли я выразить это настройкой, а не классом? —");
        System.out.println("      Если да, настройкой. Класс — только для НОВОГО ПОВЕДЕНИЯ.»");
        System.out.println();
        System.out.println("   Обратная сторона (честно):");
        System.out.println("     • реестр/фабрика — лишний слой, его надо покрыть тестами;");
        System.out.println("     • ошибка в конфигурации проявится только при старте");
        System.out.println("       (вот почему нужен стартовый smoke-тест реестра);");
        System.out.println("     • если вариантов ровно 3 и они не меняются — реестр может");
        System.out.println("       быть проще обычного switch по enum. Полиморфизм оправдан");
        System.out.println("       при РАСТУЩЕМ наборе вариантов, а не всегда.");

        System.out.println();
        System.out.println("=== 5. СРАВНИМ ОБЪЁМ ПРАВОК ===");
        System.out.println("   «ДО»:   +1 канал -> правим dispatch() И describeDelivery()");
        System.out.println("            (+ не забыть про default/else), 2 места, 1 файл.");
        System.out.println("   «ПОСЛЕ»:+1 канал -> новый файл + 1 строка register(...) в реестре.");
        System.out.println("            Старый код не трогаем ВООБЩЕ.");
        System.out.println("   Именно это и называют «открытое/закрытое» (Open/Closed):");
        System.out.println("   расширяемся ДОБАВЛЕНИЕМ, а не ПРАВКОЙ.");
    }
}
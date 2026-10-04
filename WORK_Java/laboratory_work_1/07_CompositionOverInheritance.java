import java.util.ArrayList;
import java.util.List;

/**
 * Задание 7. «Композиция вместо наследования».
 *
 * Показывает, почему «логирующий email-отправитель extends EmailSender»
 * приводит к 2^n подклассам, и как та же функциональность выражается
 * декораторами вокруг минимального контракта NotificationSender.
 * Главное наблюдение: порядок сборки цепочки задаёт наблюдаемое поведение —
 * метрики снаружи или внутри retry дают разные числа.
 *
 * Запуск: javac 07_CompositionOverInheritance.java
 *         java  -cp . CompositionOverInheritance
 */
class CompositionOverInheritance {

    /**
     * Данные уведомления. Поля package-private final, потому что
     * декораторы и отправители этого файла читают их напрямую —
     * так видно, что доступность вложенных классов шире приватной.
     */
    static class Notification {

        final String recipient;
        final String subject;
        final String body;

        /**
         * Создаёт уведомление. Вызывается из main и используется всеми
         * декораторами через ссылки в their own send(...).
         */
        Notification(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }

        /**
         * Переопределяет Object.toString() краткой формой. Используется
         * ConsoleSender и журналом LoggingSender для читаемых записей.
         */
        @Override
        public String toString() {
            return subject + " -> " + recipient;
        }
    }

    /**
     * Минимальный контракт отправителя: один метод send(...).
     * Минимальность и есть причина, почему в него удобно оборачивать
     * что угодно: декораторы реализуют тот же интерфейс.
     */
    interface NotificationSender {

        /**
         * Отправляет уведомление. Вызывается и клиентом в main, и другими
         * декораторами цепочки, которые передают вызов дальше через next.send(...).
         */
        void send(Notification n);
    }

    /**
     * Реальная отправка по email. Используется как «внутренний» элемент
     * цепочки и как объект для проверки instanceof в main.
     */
    static class EmailSender implements NotificationSender {

        private final String host;

        /**
         * Задаёт SMTP-хост. Вызывается из main при сборке цепочек
         * A, B, C, D, E, F и при проверке instanceof.
         */
        EmailSender(String host) {
            this.host = host;
        }

        /**
         * Печатает тело и адресата с указанием хоста. Вызывается либо
         * напрямую (вариант A), либо последним звеном цепочки декораторов.
         */
        @Override
        public void send(Notification n) {
            System.out.println("         [EMAIL->" + host + "] " + n.body + " (" + n.recipient + ")");
        }
    }

    /**
     * Реальная отправка в консоль. Тот же контракт, но НЕ потомок
     * EmailSender — поэтому один и тот же LoggingSender декорирует и его.
     */
    static class ConsoleSender implements NotificationSender {

        /**
         * Печатает тело и адресата в консоль. Используется как «внутренний»
         * элемент в вариантах C и H, показывая переиспользование
         * декоратора без наследования.
         */
        @Override
        public void send(Notification n) {
            System.out.println("         [CONSOLE] " + n.body + " (" + n.recipient + ")");
        }
    }

    /**
     * Базовый класс декораторов: хранит ссылку next на «внутренний»
     * отправитель. Абстрактный, потому что декоратор без добавленного
     * поведения бессмысленен.
     */
    static abstract class DecoratingSender implements NotificationSender {

        protected final NotificationSender next;

        /**
         * Принимает внутреннего отправителя и запрещает null —
         * иначе в цепочке появилась бы дыра и NPE в рантайме.
         * Вызывается из конструкторов всех трёх декораторов.
         */
        protected DecoratingSender(NotificationSender next) {
            if (next == null) {
                throw new IllegalArgumentException("в цепочке не может быть null");
            }
            this.next = next;
        }
    }

    /**
     * Декоратор логирования: реализует тот же контракт и ДЕРЖИТ другой
     * NotificationSender. Работает и с EmailSender, и с ConsoleSender,
     * и с другими декораторами.
     */
    static class LoggingSender extends DecoratingSender {

        private final List<String> log = new ArrayList<String>();

        /**
         * Оборачивает указанного отправителя. Вызывается из main в шагах
         * B, C, D, E, F, H для сборки цепочек с логированием.
         */
        LoggingSender(NotificationSender next) {
            super(next);
        }

        /**
         * Записывает START/END в консоль и журнал, между ними передаёт
         * вызов дальше через next.send(...). Вызывается клиентом в main
         * либо другим декоратором, стоящим снаружи.
         */
        @Override
        public void send(Notification n) {
            log.add("> " + n);
            System.out.println("      (log) START " + n);
            next.send(n);
            System.out.println("      (log) END   " + n);
            log.add("< " + n);
        }

        /**
         * Возвращает копию журнала. Вызывается из main в шаге H,
         * чтобы показать накопленные события без доступа к полю.
         */
        List<String> getLog() {
            return new ArrayList<String>(log);
        }
    }

    /**
     * Декоратор метрик: считает попытки и ошибки, пробрасывая исключения
     * дальше. Именно он показывает разницу порядков композиции.
     */
    static class MetricsSender extends DecoratingSender {

        private int attempts;
        private int failures;

        /**
         * Оборачивает указанного отправителя. Вызывается из main в шагах
         * D и E для двух разных порядков сборки.
         */
        MetricsSender(NotificationSender next) {
            super(next);
        }

        /**
         * Увеличивает счётчик попыток, вызывает next.send(...) и при ошибке
         * учитывает провал, перебрасывая исключение наружу: декоратор
         * наблюдает, но не подавляет сбои. Вызывается клиентом в main.
         */
        @Override
        public void send(Notification n) {
            attempts++;
            try {
                next.send(n);
            } catch (RuntimeException e) {
                failures++;
                throw e;
            }
        }

        /**
         * Возвращает накопленные счётчики одной строкой. Вызывается
         * из main в шагах D, E и G, где сравниваются числа для
         * двух порядков композиции.
         */
        @Override
        public String toString() {
            return "MetricsSender[attempts=" + attempts + ", failures=" + failures + "]";
        }
    }

    /**
     * Декоратор повторов: делает до maxAttempts попыток вызова
     * внутреннего отправителя и пробрасывает последнюю ошибку.
     */
    static class RetryingSender extends DecoratingSender {

        private final int maxAttempts;

        /**
         * Оборачивает отправителя и задаёт число попыток. Вызывается
         * из main в шагах D, E и F.
         */
        RetryingSender(NotificationSender next, int maxAttempts) {
            super(next);
            this.maxAttempts = maxAttempts;
        }

        /**
         * В цикле вызывает next.send(...), успех немедленно завершает метод,
         * ошибки запоминаются и цикл повторяется; после исчерпания попыток
         * пробрасывается последняя ошибка. Вызывается либо клиентом (шаг F),
         * либо внешним декоратором (шаги D и E).
         */
        @Override
        public void send(Notification n) {
            RuntimeException last = null;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    System.out.println("      (retry) попытка " + attempt + " из " + maxAttempts);
                    next.send(n);
                    return;
                } catch (RuntimeException e) {
                    last = e;
                    System.out.println("      (retry) неудача: " + e.getMessage());
                }
            }
            if (last != null) {
                throw last;
            }
        }
    }

    /**
     * «Капризный» email-отправитель: падает заданное число первых раз.
     * Нужен, чтобы retry и метрики были видны в выводе.
     */
    static class FlakyEmailSender implements NotificationSender {

        private final String host;
        private int leftToFail;

        /**
         * Создаёт отправитель, который упадёт failFirst раз.
         * Вызывается из main в шагах D, E и F.
         */
        FlakyEmailSender(String host, int failFirst) {
            this.host = host;
            this.leftToFail = failFirst;
        }

        /**
         * Пока счётчик сбоев не исчерпан — бросает IllegalStateException,
         * иначе печатает письмо. Именно на её исключениях настраиваются
         * RetryingSender и MetricsSender.
         */
        @Override
        public void send(Notification n) {
            if (leftToFail > 0) {
                leftToFail--;
                throw new IllegalStateException("SMTP недоступен (" + host + ")");
            }
            System.out.println("         [EMAIL->" + host + "] " + n.body + " (" + n.recipient + ")");
        }
    }

    /**
     * Точка входа. Собирает цепочки в разных порядках: без декораторов,
     * с одним логированием вокруг двух разных отправителей, с тремя
     * декораторами в трёх порядках, с журналом как самостоятельным слоем,
     * и завершается честным перечнем цен декораторов (включая провал
     * instanceof EmailSender у декорированного объекта).
     */
    public static void main(String[] args) {

        Notification n = new Notification("student@sibguti.ru", "Дедлайн", "Остался 1 день");

        System.out.println("=== A. Без декораторов: только реальный отправитель ===");
        NotificationSender plain = new EmailSender("smtp.sibguti.ru");
        plain.send(n);

        System.out.println();
        System.out.println("=== B. Один декоратор: логирование ===");
        NotificationSender logged = new LoggingSender(new EmailSender("smtp.sibguti.ru"));
        logged.send(n);

        System.out.println();
        System.out.println("=== C. Тот же логирующий декоратор, но вокруг КОНСОЛИ ===");
        NotificationSender loggedConsole = new LoggingSender(new ConsoleSender());
        loggedConsole.send(n);

        System.out.println();
        System.out.println("=== D. Три декоратора: ЛОГИРОВАНИЕ СНАРУЖИ ===");
        MetricsSender metricsD = new MetricsSender(
                new RetryingSender(new FlakyEmailSender("smtp.sibguti.ru", 2), 3));
        NotificationSender chainD = new LoggingSender(metricsD);
        chainD.send(n);
        System.out.println("   ИТОГ D: " + metricsD);

        System.out.println();
        System.out.println("=== E. Три декоратора: МЕТРИКИ СНАРУЖИ ===");
        MetricsSender metricsE = new MetricsSender(
                new LoggingSender(new RetryingSender(new FlakyEmailSender("smtp.sibguti.ru", 2), 3)));
        NotificationSender chainE = metricsE;
        chainE.send(n);
        System.out.println("   ИТОГ E: " + metricsE);

        System.out.println();
        System.out.println("=== F. РЕТРАЙ СНАРУЖИ (третий порядок, для контраста) ===");
        LoggingSender loggingF = new LoggingSender(new FlakyEmailSender("smtp.sibguti.ru", 2));
        NotificationSender chainF = new RetryingSender(loggingF, 3);
        chainF.send(n);

        System.out.println();
        System.out.println("=== G. СРАВНЕНИЕ ЧИСЕЛ (одни и те же данные, разный порядок) ===");
        System.out.println("   Вариант D (logging -> metrics -> retry -> email):");
        System.out.println("      " + metricsD + "   <- 1 логический вызов, 3 попытки СКРЫТЫ внутри retry");
        System.out.println("   Вариант E (metrics -> logging -> retry -> email):");
        System.out.println("      " + metricsE + "   <- видны ВСЕ попытки и 2 ошибки");
        System.out.println();
        System.out.println("   ВЫВОД: обе цепочки удовлетворяют ОДНОМУ контракту");
        System.out.println("   NotificationSender, но дают разное наблюдаемое поведение.");
        System.out.println("   «Кто видит чьи события» решает ПОРЯДОК СБОРКИ, а не код классов.");

        System.out.println();
        System.out.println("=== H. ЖУРНАЛ ЛОГИРОВАНИЯ как отдельный декоратор (без «побочки») ===");
        LoggingSender solo = new LoggingSender(new ConsoleSender());
        solo.send(n);
        System.out.println("   журнал:");
        for (String line : solo.getLog()) {
            System.out.println("      " + line);
        }

        System.out.println();
        System.out.println("=== I. ЧЕСТНАЯ ЦЕНА ДЕКОРАТОРОВ ===");
        System.out.println("   1) type-erasure / instanceof: декоратор — НЕ EmailSender.");
        System.out.println("      Если клиент делает if (sender instanceof EmailSender),");
        System.out.println("      декорированный отправитель туда не попадёт. Проверим:");
        NotificationSender decorated = new LoggingSender(new EmailSender("smtp.sibguti.ru"));
        System.out.println("      instanceof NotificationSender = "
                + (decorated instanceof NotificationSender) + "  (должно быть true)");
        System.out.println("      instanceof EmailSender         = "
                + (decorated instanceof EmailSender) + "  (false — и это нормально");
        System.out.println("      для декоратора; просто не проверяйте конкретные типы)");
        System.out.println("   2) цепочка растёт: 5 декораторов = 5 уровней вызовов.");
        System.out.println("   3) отладка «не в том кадре»: стек вызовов длиннее.");
        System.out.println("   Итого: декораторы дают гибкость, но требуют проектирования");
        System.out.println("   API так, чтобы клиенту НЕ НУЖНО было знать про декораторы.");
    }
}

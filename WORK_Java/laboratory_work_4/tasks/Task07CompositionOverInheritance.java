package tasks;

import common.Notification;
import common.NotificationSender;

/**
 * Задание 7. Композиция вместо наследования.
 * Дан LoggingEmailSender extends EmailSender, затем требуется retry и metrics.
 * Перепроектируйте logging и metrics как декораторы вокруг NotificationSender.
 * Покажите два порядка композиции и объясните разницу наблюдаемого поведения.
 */
public class Task07CompositionOverInheritance {

    static class FlakyEmailSender implements NotificationSender {
        private int failuresRemaining;

        FlakyEmailSender(int failuresRemaining) {
            this.failuresRemaining = failuresRemaining;
        }

        @Override
        public void send(Notification n) {
            if (failuresRemaining > 0) {
                failuresRemaining--;
                throw new RuntimeException("Временный сбой SMTP");
            }
            System.out.println("      [EmailSender] Успешно отправлено: " + n);
        }
    }

    static abstract class SenderDecorator implements NotificationSender {
        protected final NotificationSender wrappee;

        protected SenderDecorator(NotificationSender wrappee) {
            this.wrappee = wrappee;
        }
    }

    static class LoggingSenderDecorator extends SenderDecorator {
        LoggingSenderDecorator(NotificationSender wrappee) {
            super(wrappee);
        }

        @Override
        public void send(Notification n) {
            System.out.println("    [LOG] Старт отправки: " + n.getSubject());
            wrappee.send(n);
            System.out.println("    [LOG] Завершение отправки: " + n.getSubject());
        }
    }

    static class MetricsSenderDecorator extends SenderDecorator {
        private int calls;
        private int failures;

        MetricsSenderDecorator(NotificationSender wrappee) {
            super(wrappee);
        }

        @Override
        public void send(Notification n) {
            calls++;
            try {
                wrappee.send(n);
            } catch (RuntimeException e) {
                failures++;
                throw e;
            }
        }

        String stats() {
            return "calls=" + calls + ", failures=" + failures;
        }
    }

    static class RetrySenderDecorator extends SenderDecorator {
        private final int maxAttempts;

        RetrySenderDecorator(NotificationSender wrappee, int maxAttempts) {
            super(wrappee);
            this.maxAttempts = maxAttempts;
        }

        @Override
        public void send(Notification n) {
            RuntimeException lastError = null;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    System.out.println("    [RETRY] Попытка #" + attempt);
                    wrappee.send(n);
                    return;
                } catch (RuntimeException e) {
                    lastError = e;
                    System.out.println("    [RETRY] Ошибка на попытке #" + attempt + ": " + e.getMessage());
                }
            }
            throw lastError;
        }
    }

    public static void main(String[] args) {
        Notification note = new Notification("user@sibguti.ru", "Оплата курса", "Квитанция #102");

        System.out.println("=== Порядок А: Logging -> Metrics -> Retry -> Email (сбой 2 раза из 3) ===");
        MetricsSenderDecorator metricsOuter = new MetricsSenderDecorator(
                new RetrySenderDecorator(new FlakyEmailSender(2), 3)
        );
        NotificationSender orderA = new LoggingSenderDecorator(metricsOuter);
        orderA.send(note);
        System.out.println("  Метрики Порядка А (снаружи Retry): " + metricsOuter.stats());

        System.out.println("\n=== Порядок Б: Retry -> Logging -> Metrics -> Email (сбой 2 раза из 3) ===");
        MetricsSenderDecorator metricsInner = new MetricsSenderDecorator(new FlakyEmailSender(2));
        NotificationSender orderB = new RetrySenderDecorator(
                new LoggingSenderDecorator(metricsInner), 3
        );
        orderB.send(note);
        System.out.println("  Метрики Порядка Б (внутри Retry):  " + metricsInner.stats());

        System.out.println("\n=== Объяснение разницы наблюдаемого поведения ===");
        System.out.println("  - В Порядке А (Logging -> Metrics -> Retry) логгер и метрики стоят СНАРУЖИ Retry:");
        System.out.println("    наблюдается 1 клиентский вызов и 0 ошибок (повторы скрыты внутри Retry -> calls=1, failures=0).");
        System.out.println("  - В Порядке Б (Retry -> Logging -> Metrics) логгер и метрики стоят ВНУТРИ Retry:");
        System.out.println("    фиксируется каждая физическая попытка отправки и каждый сбой (calls=3, failures=2).");
    }
}

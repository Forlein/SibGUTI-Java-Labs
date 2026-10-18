package tasks;

import common.Student;
import java.util.List;

/**
 * Задание 14. Наследование взрывает комбинации.
 * Есть форматы курса Online, Classroom, Hybrid и политики оплаты Free, Fixed, Subscription.
 * Откажитесь от девяти подклассов, разделив независимые оси на компоненты.
 * Продемонстрируйте добавление нового формата и новой оплаты с минимальным изменением существующего кода.
 */
public class Task14InheritanceExplosion {

    // Ось 1: Формат проведения курса
    interface CourseFormat {
        String formatName();
        String scheduleInfo();
    }

    static class OnlineFormat implements CourseFormat {
        private final String platform;

        OnlineFormat(String platform) {
            this.platform = platform;
        }

        @Override
        public String formatName() {
            return "Online(" + platform + ")";
        }

        @Override
        public String scheduleInfo() {
            return "Вебинары онлайн + записи 24/7";
        }
    }

    static class ClassroomFormat implements CourseFormat {
        private final String room;

        ClassroomFormat(String room) {
            this.room = room;
        }

        @Override
        public String formatName() {
            return "Classroom(" + room + ")";
        }

        @Override
        public String scheduleInfo() {
            return "Очные пары в аудитории " + room;
        }
    }

    static class HybridFormat implements CourseFormat {
        @Override
        public String formatName() {
            return "Hybrid";
        }

        @Override
        public String scheduleInfo() {
            return "Лекции онлайн + лабораторные очно";
        }
    }

    // Ось 2: Политика оплаты курса
    interface PaymentPolicy {
        String policyName();
        int calculatePrice(Student student);
    }

    static class FreePayment implements PaymentPolicy {
        @Override
        public String policyName() {
            return "Free";
        }

        @Override
        public int calculatePrice(Student student) {
            return 0;
        }
    }

    static class FixedPayment implements PaymentPolicy {
        private final int amount;

        FixedPayment(int amount) {
            this.amount = amount;
        }

        @Override
        public String policyName() {
            return "Fixed(" + amount + " руб.)";
        }

        @Override
        public int calculatePrice(Student student) {
            return amount;
        }
    }

    static class SubscriptionPayment implements PaymentPolicy {
        private final int monthlyRate;

        SubscriptionPayment(int monthlyRate) {
            this.monthlyRate = monthlyRate;
        }

        @Override
        public String policyName() {
            return "Subscription(" + monthlyRate + " руб./мес)";
        }

        @Override
        public int calculatePrice(Student student) {
            return student.hasBenefit() ? (int) (monthlyRate * 0.8) : monthlyRate;
        }
    }

    // Новая реализация формата (добавлена без изменения существующих классов)
    static class OnDemandFormat implements CourseFormat {
        @Override
        public String formatName() {
            return "OnDemand";
        }

        @Override
        public String scheduleInfo() {
            return "Самостоятельное прохождение в своём темпе";
        }
    }

    // Новая реализация оплаты (добавлена без изменения существующих классов)
    static class InstallmentPayment implements PaymentPolicy {
        private final int total;
        private final int months;

        InstallmentPayment(int total, int months) {
            this.total = total;
            this.months = months;
        }

        @Override
        public String policyName() {
            return "Installment(" + total + " руб. на " + months + " мес.)";
        }

        @Override
        public int calculatePrice(Student student) {
            return total / months;
        }
    }

    // Курс объединяет две независимые оси через композицию (вместо 9 или 16 подклассов)
    static class CourseOffering {
        private final String code;
        private final String title;
        private final CourseFormat format;
        private final PaymentPolicy payment;

        CourseOffering(String code, String title, CourseFormat format, PaymentPolicy payment) {
            this.code = code;
            this.title = title;
            this.format = format;
            this.payment = payment;
        }

        public String describeFor(Student student) {
            return String.format("%-8s | %-20s | %-22s | %-34s | к оплате: %d руб.",
                    code, title, format.formatName(), payment.policyName(), payment.calculatePrice(student));
        }
    }

    public static void main(String[] args) {
        Student student = new Student("STU-1", "Мария Кузнецова", 20, true);

        List<CourseFormat> baseFormats = List.of(
                new OnlineFormat("MTS Link"),
                new ClassroomFormat("ауд. 412"),
                new HybridFormat()
        );
        List<PaymentPolicy> basePayments = List.of(
                new FreePayment(),
                new FixedPayment(30000),
                new SubscriptionPayment(4500)
        );

        System.out.println("=== 1. Девять комбинаций (3 формата x 3 политики оплаты) через композицию без 9 подклассов ===");
        int idx = 1;
        for (CourseFormat fmt : baseFormats) {
            for (PaymentPolicy pay : basePayments) {
                CourseOffering offering = new CourseOffering("CRS-" + idx, "Курс #" + idx, fmt, pay);
                System.out.println("  " + offering.describeFor(student));
                idx++;
            }
        }

        System.out.println("\n=== 2. Добавление нового формата (OnDemand) и новой оплаты (Installment) без правок старого кода ===");
        CourseFormat newFormat = new OnDemandFormat();
        PaymentPolicy newPayment = new InstallmentPayment(36000, 6);

        CourseOffering combo1 = new CourseOffering("NEW-1", "Java Архитектура", newFormat, basePayments.get(1));
        CourseOffering combo2 = new CourseOffering("NEW-2", "Сети и Телеком", baseFormats.get(1), newPayment);
        CourseOffering combo3 = new CourseOffering("NEW-3", "DevOps Практикум", newFormat, newPayment);

        System.out.println("  " + combo1.describeFor(student));
        System.out.println("  " + combo2.describeFor(student));
        System.out.println("  " + combo3.describeFor(student));
    }
}

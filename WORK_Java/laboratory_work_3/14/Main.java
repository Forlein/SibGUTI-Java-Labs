// Ось 1: Формат
interface Format { String name(); }
class Online implements Format { public String name() { return "Online"; } }
class Classroom implements Format { public String name() { return "Classroom"; } }
class Hybrid implements Format { public String name() { return "Hybrid"; } }

// Ось 2: Оплата
interface Payment { String name(); }
class Free implements Payment { public String name() { return "Free"; } }
class Fixed implements Payment { public String name() { return "Fixed"; } }
class Subscription implements Payment { public String name() { return "Subscription"; } }

// Сам курс (собирает две оси)
class Course {
    String title; Format format; Payment payment;

    Course(String title, Format format, Payment payment) {
        this.title = title; this.format = format; this.payment = payment;
    }

    void printInfo() {
        System.out.println(title + " | " + format.name() + " | " + payment.name());
    }
}

public class Main {
    public static void main(String[] args) {
        // 1. Существующие комбинации
        new Course("Java", new Online(), new Free()).printInfo();
        new Course("Python", new Classroom(), new Subscription()).printInfo();

        // 2. Добавляем новый формат (VR) — всего 1 класс
        Format vr = new Format() { public String name() { return "VR"; } };
        new Course("C#", vr, new Free()).printInfo();

        // 3. Добавляем новую оплату (Crypto) — всего 1 класс
        Payment crypto = new Payment() { public String name() { return "Crypto"; } };
        new Course("Rust", new Online(), crypto).printInfo();
    }
}
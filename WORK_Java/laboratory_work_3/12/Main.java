// 1. Запечатанный интерфейс (разрешены ТОЛЬКО эти три класса)
sealed interface EnrollmentResult permits Accepted, Rejected, WaitListed {}

// 2. Разрешенные реализации
record Accepted(String courseName) implements EnrollmentResult {}
record Rejected(String reason) implements EnrollmentResult {}
record WaitListed(int position) implements EnrollmentResult {}

public class Main {
    public static void main(String[] args) {
        EnrollmentResult result1 = new Accepted("Java");
        EnrollmentResult result2 = new Rejected("Нет мест");
        EnrollmentResult result3 = new WaitListed(5);

        checkResult(result1);
        checkResult(result2);
        checkResult(result3);
    }

    // 3. Exhaustive switch (исчерпывающий)
    public static void checkResult(EnrollmentResult result) {
        switch (result) {
            case Accepted a -> System.out.println("Успех! Вы записаны на " + a.courseName());
            case Rejected r -> System.out.println("Отказ. Причина: " + r.reason());
            case WaitListed w -> System.out.println("В листе ожидания. Ваш номер: " + w.position());
            
            // ОБРАТИ ВНИМАНИЕ: тут НЕТ default!
            // Компилятор сам знает, что других вариантов быть не может.
        }
    }
}
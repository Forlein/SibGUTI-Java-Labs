/*
Задание 7. Ошибка как значение или исключение
Для `findCourse`, `parseCourse`, `enroll`, `calculatePrice` 
выберите `Optional`, типизированный result, checked или 
unchecked exception. Реализуйте два контрастных случая и 
защитите границу ожидаемого результата.
*/
import java.util.Optional;

public class Main {

    // 1. findCourse -> Optional (не нашли = не ошибка)
    static Optional<String> findCourse(String code) {
        return code.equals("JAVA") ? Optional.of("Java Course") : Optional.empty();
    }

    // 2. parseCourse -> checked (данные извне, можно обработать)
    static class ParseException extends Exception {
        ParseException(String msg) { super(msg); }
    }
    static int parseCourse(String s) throws ParseException {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { throw new ParseException("bad course: " + s); }
    }

    // 3. enroll -> checked (ожидаемый отказ)
    static class EnrollmentException extends Exception {
        EnrollmentException(String msg) { super(msg); }
    }
    static void enroll(String studentId, boolean hasSeats) throws EnrollmentException {
        if (!hasSeats) throw new EnrollmentException("no seats");
        System.out.println("enrolled " + studentId);
    }

    // 4. calculatePrice -> unchecked (баг вызывающего)
    static int calculatePrice(int base, int discount) {
        if (base < 0 || discount < 0 || discount > 100)
            throw new IllegalArgumentException("bad price args");
        return base * (100 - discount) / 100;
    }

    public static void main(String[] args) {
        // контраст 1: Optional — пусто не ошибка
        System.out.println("find JAVA  = " + findCourse("JAVA"));
        System.out.println("find XXX   = " + findCourse("XXX"));

        // контраст 2: checked — ожидаемая ошибка, ловим
        try { enroll("s1", false); }
        catch (EnrollmentException e) { System.out.println("enroll: " + e.getMessage()); }

        // unchecked — баг, не ловим в обычном коде (тут ловим для демо)
        try { calculatePrice(-10, 5); }
        catch (IllegalArgumentException e) { System.out.println("price: " + e.getMessage()); }

        // checked parse — ловим
        try { System.out.println("parse = " + parseCourse("abc")); }
        catch (ParseException e) { System.out.println("parse: " + e.getMessage()); }
    }
}

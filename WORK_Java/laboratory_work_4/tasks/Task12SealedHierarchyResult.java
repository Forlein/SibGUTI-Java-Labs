package tasks;

/**
 * Задание 12. Sealed-иерархия результата.
 * Создайте sealed interface EnrollmentResult с Accepted, Rejected и WaitListed.
 * Обработайте все варианты через exhaustive switch без default.
 * Добавьте новый permitted-тип и зафиксируйте, где компилятор потребовал изменение.
 */
public class Task12SealedHierarchyResult {

    // В permits добавлен четвёртый тип RequiresApproval (шаг расширения иерархии)
    sealed interface EnrollmentResult
            permits Accepted, Rejected, WaitListed, RequiresApproval {
    }

    record Accepted(String studentId, String courseCode) implements EnrollmentResult {}

    record Rejected(String studentId, String reason) implements EnrollmentResult {}

    record WaitListed(String studentId, int position) implements EnrollmentResult {}

    // Новый добавленный permitted-тип:
    record RequiresApproval(String studentId, String advisorName) implements EnrollmentResult {}

    /**
     * Исчерпывающий (exhaustive) switch без ветки default.
     * При добавлении RequiresApproval в permits компилятор выдал ошибку ровно в этом switch:
     * "error: the switch expression does not cover all possible input values",
     * потребовав добавить ветку case RequiresApproval.
     */
    public static String formatResult(EnrollmentResult result) {
        return switch (result) {
            case Accepted a ->
                    "ЗАЧИСЛЕН        : студент " + a.studentId() + " на курс " + a.courseCode();
            case Rejected r ->
                    "ОТКАЗ           : студент " + r.studentId() + " (причина: " + r.reason() + ")";
            case WaitListed w ->
                    "ЛИСТ ОЖИДАНИЯ   : студент " + w.studentId() + " (позиция #" + w.position() + ")";
            case RequiresApproval req ->
                    "НУЖНО ОДОБРЕНИЕ : студент " + req.studentId() + " (куратор: " + req.advisorName() + ")";
        };
    }

    public static void main(String[] args) {
        EnrollmentResult[] results = {
                new Accepted("STU-01", "JAVA-4"),
                new Rejected("STU-02", "Не сдан пререквизит JAVA-3"),
                new WaitListed("STU-03", 1),
                new RequiresApproval("STU-04", "проф. Соколов")
        };

        System.out.println("=== 1. Обработка всех вариантов через exhaustive switch без default ===");
        for (EnrollmentResult r : results) {
            System.out.println("  " + formatResult(r));
        }

        System.out.println("\n=== 2. Фиксация реакции компилятора при добавлении нового permitted-типа ===");
        System.out.println("  1) Добавлен record RequiresApproval в секцию permits интерфейса EnrollmentResult.");
        System.out.println("  2) При компиляции без обновления switch в методе formatResult(EnrollmentResult)");
        System.out.println("     компилятор указал на строку 'return switch (result)' с ошибкой:");
        System.out.println("     'error: the switch expression does not cover all possible input values'.");
        System.out.println("  3) После добавления ветки 'case RequiresApproval req -> ...' компиляция прошла успешно.");
    }
}

/*
Задание 3. Собственное исключение
Создайте `EnrollmentRejectedException` с `studentId`, 
`courseId` и reason code. Сохраните исходную причину 
при оборачивании. `toString` и сообщение не раскрывают 
лишние данные.
*/
public class Main {

    static class EnrollmentRejectedException extends Exception {
        final String studentId, courseId, reasonCode;

        EnrollmentRejectedException(String studentId, String courseId, String reasonCode, Throwable cause) {
            super("Rejected: " + reasonCode, cause);
            this.studentId = studentId;
            this.courseId = courseId;
            this.reasonCode = reasonCode;
        }

        @Override public String toString() { return "EnrollmentRejectedException[" + reasonCode + "]"; }
    }

    static void enroll(String studentId, String courseId) throws EnrollmentRejectedException {
        try {
            throw new IllegalStateException("нет пререквизита");
        } catch (IllegalStateException e) {
            throw new EnrollmentRejectedException(studentId, courseId, "PREREQ_MISSING", e);
        }
    }

    public static void main(String[] args) {
        try {
            enroll("12345678", "JAVA-101");
        } catch (EnrollmentRejectedException e) {
            System.out.println(e.getMessage());        // без id
            System.out.println(e);                     // без id
            System.out.println(e.getCause());          // причина
            System.out.println(e.studentId + " " + e.courseId + " " + e.reasonCode); // поля
        }
    }
}

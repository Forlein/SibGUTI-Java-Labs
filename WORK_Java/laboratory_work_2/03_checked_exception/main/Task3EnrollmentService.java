package ru.sibguti.lab2.task03;

import ru.sibguti.lab2.Console;
import ru.sibguti.lab2.domain.CourseCode;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.ErrorCode;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 3, перенесён в задание 3 без изменений.
 * Зачем он нужен в этом задании: демонстрация checked-исключения зачисления, состояние курса после отказа.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Задание 3. Собственное исключение (продолжение).
 * Класс Task3EnrollmentService показывает, как низкоуровневая ошибка
 * оборачивается в доменное исключение EnrollmentRejectedException:
 * код причины выбирается по типу исходной ошибки, а сама исходная ошибка
 * сохраняется в cause и не теряется. Здесь же демонстрируется, что
 * сообщение и toString не раскрывают внутренних подробностей.
 * Зависимости: использует CourseStatus и ErrorCode из пакета domain,
 * создаёт и бросает EnrollmentRejectedException, который ловит main.
 */
public class Task3EnrollmentService {

    /*
     * Главный метод показывает четыре сценария отказа: нет мест, курс закрыт,
     * повторная заявка и поломка инфраструктуры. Его вызывает среда разработки.
     * Первые три отказа - бизнес-правила (cause нет), четвёртый - обёрнутая
     * низкоуровневая ошибка (cause сохранён).
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 3. СОБСТВЕННОЕ ИСКЛЮЧЕНИЕ");
        System.out.println();

        Task3EnrollmentService service = new Task3EnrollmentService();
        showRejectedEnrollment(service, "S-01", "CS101", 0, CourseStatus.OPEN, false);
        showRejectedEnrollment(service, "S-02", "CS102", 10, CourseStatus.CLOSED, false);
        showRejectedEnrollment(service, "S-03", "CS103", 10, CourseStatus.OPEN, true);

        System.out.println("Тот же доменный отказ, но вызванный поломкой инфраструктуры: "
                + "исходная причина сохраняется в cause и не теряется.");
        showRejectedEnrollment(new Task3EnrollmentService(true), "S-04", "CS104", 10, CourseStatus.OPEN, false);
    }

    /*
     * Низкоуровневое исключение SeatAllocationException: его сообщение
     * содержит служебные подробности склада мест (имя внутреннего сервиса,
     * код ответа), которые не должны попасть к пользователю. Именно поэтому
     * при оборачивании оно уходит в cause, а не в message.
     * Его бросает метод tryReserveSeat, ловит enroll.
     */
    public static class SeatAllocationException extends Exception {

        private static final long serialVersionUID = 1L;

        public SeatAllocationException(String internalDetails) {
            super("Склад мест недоступен: " + internalDetails);
        }
    }

    private final boolean failInfrastructure;

    /*
     * Конструктор без параметров создаёт обычный сервис, у которого склад мест
     * работает. Его вызывает main для трёх бизнес-отказов.
     */
    public Task3EnrollmentService() {
        this(false);
    }

    /*
     * Конструктор с флагом failInfrastructure включает имитацию поломки склада мест.
     * Его вызывает main, чтобы показать оборачивание с сохранением cause,
     * и создают его тесты задания 11 для проверки полей исключения.
     */
    public Task3EnrollmentService(boolean failInfrastructure) {
        this.failInfrastructure = failInfrastructure;
    }

    /*
     * Метод enroll - основной метод задания. Он проверяет код курса, потом
     * бизнес-правила и только в конце обращается к инфраструктуре.
     * Любой отказ наружу отдаётся одним типом EnrollmentRejectedException:
     * код причины берётся из ErrorCode, исходная ошибка (если была) уходит в cause.
     * Его вызывают main и метод showRejectedEnrollment, а наружу метод
     * объявляет checked-исключение, поэтому вызывающий обязан его обработать.
     */
    public void enroll(String studentId, String rawCourseCode, int freeSeats, CourseStatus status,
                       boolean alreadyApplied) throws EnrollmentRejectedException {
        CourseCode code;
        try {
            code = CourseCode.of(rawCourseCode);
        } catch (IllegalArgumentException exception) {
            throw new EnrollmentRejectedException(ErrorCode.INVALID_COURSE_CODE, studentId, rawCourseCode, exception);
        }

        if (alreadyApplied) {
            throw new EnrollmentRejectedException(ErrorCode.ALREADY_ENROLLED, studentId, code.value());
        }
        if (status != CourseStatus.OPEN) {
            throw new EnrollmentRejectedException(ErrorCode.COURSE_NOT_OPEN, studentId, code.value());
        }
        if (freeSeats <= 0) {
            throw new EnrollmentRejectedException(ErrorCode.NO_FREE_SEATS, studentId, code.value());
        }

        try {
            tryReserveSeat(code.value(), studentId);
        } catch (SeatAllocationException exception) {
            throw new EnrollmentRejectedException(ErrorCode.SERVICE_UNAVAILABLE, studentId, code.value(), exception);
        }

        System.out.println("Студент " + studentId + " зачислен на курс " + code.value());
    }

    /*
     * Метод tryReserveSeat имитирует обращение к инфраструктуре склада мест.
     * Он объявляет checked-исключение SeatAllocationException, потому что это
     * внешний отказ, который может повториться. Его вызывает enroll,
     * и только enroll решает, как превратить его в доменный отказ.
     */
    private void tryReserveSeat(String courseId, String studentId) throws SeatAllocationException {
        if (failInfrastructure) {
            throw new SeatAllocationException("seat-service, HTTP 503, tenant=prod");
        }
        System.out.println("Место для " + studentId + " в курсе " + courseId + " успешно зарезервировано");
    }

    /*
     * Метод showRejectedEnrollment вызывает enroll в блоке try, ловит
     * EnrollmentRejectedException и печатает все его публичные данные:
     * сообщение, код причины, идентификаторы и cause. Так видно, что причина
     * сохранена (cause не пуст), но её текст не попал в message и в toString.
     * Его вызывает main для каждого из четырёх сценариев отказа.
     */
    private static void showRejectedEnrollment(Task3EnrollmentService service, String studentId, String rawCourseCode,
                                               int freeSeats, CourseStatus status, boolean alreadyApplied) {
        try {
            service.enroll(studentId, rawCourseCode, freeSeats, status, alreadyApplied);
        } catch (EnrollmentRejectedException exception) {
            System.out.println("Поймано: " + exception);
            System.out.println("   message         : " + exception.getMessage());
            System.out.println("   reasonCode      : " + exception.getReasonCode().code());
            System.out.println("   studentId       : " + exception.getStudentId());
            System.out.println("   courseId        : " + exception.getCourseId());
            Throwable cause = exception.getCause();
            if (cause == null) {
                System.out.println("   cause           : нет, отказ бизнес-правила");
            } else {
                System.out.println("   cause (тип)     : " + cause.getClass().getName());
                System.out.println("   cause (секретно): " + cause.getMessage());
            }
            System.out.println();
        }
    }
}

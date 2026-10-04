package ru.sibguti.lab2.task11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.task03.EnrollmentRejectedException;
import ru.sibguti.lab2.task03.Task3EnrollmentService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 11. assertThrows и состояние после отказа.
 * Класс Task11RejectionStateTest проверяет не тип исключения, а три вещи сразу:
 * сам тип, содержимое исключения (сообщение и поля) и неизменность объекта
 * после неуспешной команды. Одной проверки assertThrows мало: она скажет
 * "что-то было поймано", но не скажет, что объект остался целым.
 * Во всех assertThrows в lambda попадает ровно одно действие, иначе при падении
 * непонятно, какое именно действие её вызвало.
 * Зависимости: использует доменный Course и EnrollmentRejectedException
 * из задания 3 вместе с Task3EnrollmentService.
 */
class Task11RejectionStateTest {

    /*
     * Тест проверяет отказ completeHours при попытке закрыть больше часов,
     * чем осталось. В lambda только одно действие - вызов completeHours,
     * а проверки состояния вынесены наружу и собраны в assertAll.
     */
    @Test
    @DisplayName("completeHours отказывает и не меняет состояние курса")
    void completeHoursRejectsOverflowWithoutChangingState() {
        Course course = new Course("CS101", "Основы Java", 72, 30);
        course.completeHours(50);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> course.completeHours(50),
                "нельзя закрыть больше часов, чем осталось в курсе");

        assertAll(
                () -> assertEquals("Course: нельзя закрыть 50 ч., доступно только 22 ч.",
                        exception.getMessage(), "сообщение должно называть остаток часов"),
                () -> assertEquals(50, course.getCompletedHours(),
                        "после отказа число закрытых часов не должно меняться"),
                () -> assertEquals(22, course.remainingHours(),
                        "остаток часов после отказа тоже не должен меняться"),
                () -> assertFalse(course.isCompleted(),
                        "отказ не должен делать курс завершённым")
        );
    }

    /*
     * Тест проверяет доменный отказ зачисления: у исключения должны быть
     * код причины, идентификатор студента и код курса. В lambda только
     * один вызов enroll, а все проверки полей вынесены наружу.
     */
    @Test
    @DisplayName("отказ зачисления содержит код причины, студента и курс")
    void enrollmentRejectionCarriesReasonAndStudentData() {
        Task3EnrollmentService service = new Task3EnrollmentService();

        EnrollmentRejectedException exception = assertThrows(EnrollmentRejectedException.class,
                () -> service.enroll("S-01", "CS101", 0, CourseStatus.OPEN, false),
                "при нуле свободных мест зачисление должно быть отклонено");

        assertAll(
                () -> assertEquals(ErrorCode.NO_FREE_SEATS, exception.getReasonCode(),
                        "код причины отказа"),
                () -> assertEquals("S-01", exception.getStudentId(), "идентификатор студента"),
                () -> assertEquals("CS101", exception.getCourseId(), "код курса"),
                () -> assertTrue(exception.getMessage().contains("E104"),
                        "сообщение должно содержать стабильный код причины"),
                () -> assertNull(exception.getCause(),
                        "бизнес-отказ не имеет низкоуровневой причины")
        );
    }

    /*
     * Тест проверяет, что отказ из-за поломки инфраструктуры сохраняет
     * исходную причину в cause, но не выдаёт её текст наружу: в сообщении
     * доменного исключения нет служебных подробностей склада мест.
     */
    @Test
    @DisplayName("обёрнутая ошибка сохраняет cause и не выдаёт внутренние детали")
    void wrappedInfrastructureFailureKeepsCauseButHidesDetails() {
        Task3EnrollmentService service = new Task3EnrollmentService(true);

        EnrollmentRejectedException exception = assertThrows(EnrollmentRejectedException.class,
                () -> service.enroll("S-02", "CS102", 5, CourseStatus.OPEN, false),
                "поломка инфраструктуры должна превращаться в доменный отказ");

        assertAll(
                () -> assertEquals(ErrorCode.SERVICE_UNAVAILABLE, exception.getReasonCode(),
                        "код причины инфраструктурного отказа"),
                () -> assertNotNull(exception.getCause(),
                        "исходная причина не должна теряться при оборачивании"),
                () -> assertEquals(Task3EnrollmentService.SeatAllocationException.class,
                        exception.getCause().getClass(),
                        "исходная причина должна сохранить свой тип"),
                () -> assertFalse(exception.getMessage().contains("HTTP 503"),
                        "внутренние подробности не должны попадать в message")
        );
    }

    /*
     * Тест проверяет, что программная ошибка в коде курса не теряется:
     * доменный отказ ссылается на исходное IllegalArgumentException в cause,
     * поэтому разобрать причину можно по цепочке, а не по догадкам.
     */
    @Test
    @DisplayName("неверный код курса сохраняет исходную ошибку в cause")
    void invalidCourseCodeKeepsOriginalIllegalArgument() {
        Task3EnrollmentService service = new Task3EnrollmentService();

        EnrollmentRejectedException exception = assertThrows(EnrollmentRejectedException.class,
                () -> service.enroll("S-03", "101", 5, CourseStatus.OPEN, false),
                "код без букв должен быть отклонён");

        assertAll(
                () -> assertEquals(ErrorCode.INVALID_COURSE_CODE, exception.getReasonCode(),
                        "код причины для неверного кода"),
                () -> assertEquals(IllegalArgumentException.class, exception.getCause().getClass(),
                        "исходная ошибка разбора кода должна остаться в cause"),
                () -> assertFalse(exception.getCause().getMessage().isEmpty(),
                        "сообщение исходной ошибки должно быть непустым")
        );
    }
}

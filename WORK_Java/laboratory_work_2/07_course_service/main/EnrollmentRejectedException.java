package ru.sibguti.lab2.task03;

import ru.sibguti.lab2.domain.ErrorCode;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 3, перенесён в задание 7 без изменений.
 * Зачем он нужен в этом задании: checked-отказ зачисления с кодом, идентификатором студента и курса.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Задание 3. Собственное исключение.
 * Класс EnrollmentRejectedException - доменное исключение «зачисление отклонено».
 * Оно хранит три вещи, которые нужны для разбора отказа: studentId, courseId
 * и reason code (стабильный код причины из общего контракта ErrorCode),
 * а исходную причину (cause) сохраняет через стандартный механизм Throwable.
 * Класс наследуется от Exception, то есть является checked: отказ зачисления -
 * это бизнес-результат, о котором вызывающий обязан явно решить, а не спрятать.
 * Сообщение и toString намеренно не показывают ничего лишнего: ни текста
 * исходной причины (там может быть служебная информация), ни стека, ни внутренних
 * полей; исходная причина доступна только через getCause().
 * Зависимости: использует ErrorCode из пакета domain (единый контракт кодов
 * ошибок для заданий 3, 13, 14, 15); создаёт его Task3EnrollmentService при
 * оборачивании низкоуровневой ошибки, а читает его код обработчик на границе.
 */
public class EnrollmentRejectedException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String studentId;
    private final String courseId;
    private final ErrorCode reasonCode;

    public EnrollmentRejectedException(ErrorCode reasonCode, String studentId, String courseId) {
        this(reasonCode, studentId, courseId, null);
    }

    public EnrollmentRejectedException(ErrorCode reasonCode, String studentId, String courseId, Throwable cause) {
        super(buildMessage(reasonCode, studentId, courseId), cause);
        this.reasonCode = reasonCode;
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public ErrorCode getReasonCode() {
        return reasonCode;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public String toString() {
        return "EnrollmentRejectedException[reason=" + reasonCode.code()
                + ", studentId=" + studentId
                + ", courseId=" + courseId + "]";
    }

    /*
     * Метод buildMessage собирает короткое сообщение только из данных,
     * которые и так есть в полях исключения: кода причины и идентификаторов.
     * Его вызывают оба конструктора класса через super(...).
     * Текст исходной причины сюда намеренно не попадает, чтобы служебные
     * подробности нижнего уровня не всплывали на границу.
     */
    private static String buildMessage(ErrorCode reasonCode, String studentId, String courseId) {
        return "Зачисление отклонено: reason=" + reasonCode.code()
                + ", studentId=" + studentId
                + ", courseId=" + courseId;
    }
}

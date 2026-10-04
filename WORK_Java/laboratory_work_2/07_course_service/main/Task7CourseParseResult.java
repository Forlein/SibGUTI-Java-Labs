package ru.sibguti.lab2.task07;

import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.ErrorCode;

/*
 * Задание 7. Ошибка как значение (часть 1).
 * Класс Task7CourseParseResult - типизированный результат разбора одной записи.
 * Он выбран для операции parseCourse потому, что при импорте файла нужно
 * накапливать ошибки по каждой записи и показывать отчёт, а не падать
 * на первой же плохой строке. Исключений здесь нет вообще: есть объект,
 * у которого либо готовый Course, либо стабильный код ErrorCode.
 * Зависимости: создают его методы parseCourse класса Task7CourseService,
 * читают его обработчик импорта и тесты задания 7.
 */
public class Task7CourseParseResult {

    private final boolean success;
    private final Course course;
    private final int recordNumber;
    private final ErrorCode errorCode;
    private final String errorMessage;

    private Task7CourseParseResult(boolean success, Course course, int recordNumber,
                                    ErrorCode errorCode, String errorMessage) {
        this.success = success;
        this.course = course;
        this.recordNumber = recordNumber;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static Task7CourseParseResult ok(Course course, int recordNumber) {
        return new Task7CourseParseResult(true, course, recordNumber, null, null);
    }

    public static Task7CourseParseResult fail(int recordNumber, ErrorCode errorCode, String errorMessage) {
        return new Task7CourseParseResult(false, null, recordNumber, errorCode, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public Course getCourse() {
        return course;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String toString() {
        if (success) {
            return "OK[" + course.getCode().value() + "] запись №" + recordNumber;
        }
        return "FAIL[" + errorCode.code() + "] запись №" + recordNumber + ": " + errorMessage;
    }
}

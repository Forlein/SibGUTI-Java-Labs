package ru.sibguti.lab2.task06;

/*
 * Задание 6. Граница перевода исключения (часть 1).
 * Класс CourseImportException - доменное исключение уровня импорта.
 * Оно checked, потому что сбой импорта обязан обработать вызывающий:
 * либо опубликовать уже разобранное, либо откатить импорт целиком.
 * Исключение хранит номер записи и имя поля, где произошла ошибка,
 * а исходная низкоуровневая ошибка сохраняется в cause.
 * Зависимости: создаёт его Task6ImportService при переводе
 * NumberFormatException из парсера; читают его обработчики на границе.
 */
public class CourseImportException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int recordNumber;
    private final String fieldName;

    public CourseImportException(int recordNumber, String fieldName, String message, Throwable cause) {
        super(buildMessage(recordNumber, fieldName, message), cause);
        this.recordNumber = recordNumber;
        this.fieldName = fieldName;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String toString() {
        return "CourseImportException[record=" + recordNumber + ", field=" + fieldName + "]";
    }

    /*
     * Метод buildMessage собирает сообщение из номера записи, имени поля
     * и короткого объяснения. Его вызывает конструктор через super(...).
     * Сюда не попадает текст cause: детали низкого уровня остаются
     * доступны только через getCause() - так контекст сохраняется,
     * но не выгружается наружу целиком.
     */
    private static String buildMessage(int recordNumber, String fieldName, String message) {
        return "Импорт не выполнен: запись №" + recordNumber + ", поле \"" + fieldName + "\": " + message;
    }
}

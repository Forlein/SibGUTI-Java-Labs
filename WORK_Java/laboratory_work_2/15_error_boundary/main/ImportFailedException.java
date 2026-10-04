package ru.sibguti.lab2.task14;

import ru.sibguti.lab2.domain.ErrorCode;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 15 без изменений.
 * Зачем он нужен в этом задании: checked-отказ импорта с кодом, номером записи и числом разобранных записей.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Задание 14. Ресурс с двумя отказами (часть 1).
 * Класс ImportFailedException - единственное исключение, которое импортёр
 * задания 14 отдаёт наружу. Оно checked и несёт три факта: стабильный код
 * отказа, номер записи (или -1, если отказ не связан с записью) и число
 * записей, которые успели разобрать до отказа. Исходная причина всегда
 * сохраняется в cause, а ошибки закрытия ресурса попадают туда же
 * через getSuppressed() - их добавляет try-with-resources.
 * Зависимости: его бросает CourseImporter, ловит и переводит в код ошибки
 * публичный слой CourseHubFacade задания 15.
 */
public class ImportFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    private static final int NO_RECORD_NUMBER = -1;

    private final ErrorCode errorCode;
    private final int recordNumber;
    private final int recordsProcessed;

    public ImportFailedException(ErrorCode errorCode, int recordNumber, int recordsProcessed, Throwable cause) {
        super(buildMessage(errorCode, recordNumber, recordsProcessed), cause);
        this.errorCode = errorCode;
        this.recordNumber = recordNumber;
        this.recordsProcessed = recordsProcessed;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public String toString() {
        return "ImportFailedException[code=" + errorCode.code()
                + ", record=" + recordNumber
                + ", processed=" + recordsProcessed + "]";
    }

    /*
     * Метод buildMessage собирает короткое сообщение с кодом, номером записи
     * и числом разобранных записей. Его вызывает конструктор через super(...).
     * Текст cause сюда не попадает: он остаётся доступен через getCause().
     */
    private static String buildMessage(ErrorCode errorCode, int recordNumber, int recordsProcessed) {
        String record = recordNumber == NO_RECORD_NUMBER
                ? "источник целиком"
                : "запись №" + recordNumber;
        return "Импорт прерван (" + errorCode.code() + "): " + record
                + ", успешно разобрано записей: " + recordsProcessed;
    }
}

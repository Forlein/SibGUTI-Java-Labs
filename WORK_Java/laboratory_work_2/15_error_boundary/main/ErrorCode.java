package ru.sibguti.lab2.domain;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 3, перенесён в задание 15 без изменений.
 * Зачем он нужен в этом задании: единый контракт кодов ошибок E101..E900, который видят клиенты.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Перечисление ErrorCode — это публичный контракт ошибок CourseHub (задания 13, 14, 15).
 * Каждой ошибке задан неизменяемый строковый код вида "E100": код не меняется
 * при переименовании enum-константы или класса, поэтому клиенты (UI, скрипты, логи)
 * могут сравнивать его строкой и не ломаются после рефакторинга.
 * Зависимости: ErrorCode использует EnrollmentService в таблице решений (задание 13)
 * и CourseHubFacade, который возвращает эти коды наружу вместо исключений (задание 15).
 */
public enum ErrorCode {

    INVALID_COURSE_CODE("E101", "код курса не соответствует формату"),
    COURSE_NOT_FOUND("E102", "курс с таким кодом не найден"),
    COURSE_NOT_OPEN("E103", "курс не открыт для зачисления"),
    NO_FREE_SEATS("E104", "в курсе закончились свободные места"),
    PREREQUISITE_MISSING("E105", "не выполнена предварительная дисциплина"),
    ALREADY_ENROLLED("E106", "студент уже подал заявку на этот курс"),
    HOURS_OUT_OF_RANGE("E108", "количество часов вне допустимого диапазона"),
    MALFORMED_RECORD("E109", "запись в файле повреждена или не соответствует формату"),
    IMPORT_SOURCE_UNREADABLE("E110", "источник импорта не удалось прочитать"),
    IMPORT_INCOMPLETE("E111", "импорт прерван, результат не опубликован"),
    SERVICE_UNAVAILABLE("E500", "внутренний сервис временно недоступен"),
    INTERNAL_ERROR("E900", "непредвиденная программная ошибка");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}

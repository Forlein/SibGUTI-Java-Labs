package ru.sibguti.lab2.domain;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 3, перенесён в задание 11 без изменений.
 * Зачем он нужен в этом задании: четыре состояния курса, из которых зачисление разрешает только OPEN.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Перечисление CourseStatus хранит все состояния, в которых может находиться курс.
 * Зависимости: его читает EnrollmentService (задание 13) при построении
 * таблицы решений и CourseHubFacade (задание 15) при зачислении студента.
 * Состояний ровно четыре, и только одно из них разрешает зачисление.
 */
public enum CourseStatus {

    NEW("курс создан, зачисление ещё не открыто"),
    OPEN("идёт набор, зачисление разрешено"),
    CLOSED("набор закрыт"),
    CANCELLED("курс отменён");

    private final String description;

    CourseStatus(String description) {
        this.description = description;
    }

    public boolean acceptsEnrollment() {
        return this == OPEN;
    }

    public String description() {
        return description;
    }
}

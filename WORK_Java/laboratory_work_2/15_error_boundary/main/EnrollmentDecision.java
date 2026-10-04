package ru.sibguti.lab2.domain;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 7, перенесён в задание 15 без изменений.
 * Зачем он нужен в этом задании: результат таблицы решений: зачисление разрешено или отказ с кодом и номером правила.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс EnrollmentDecision — результат одного решения таблицы решений из задания 13.
 * Он не бросает исключение, а возвращает данные: принято ли зачисление,
 * какой стабильный код ошибки сработал и по какому правилу (rule) принято решение.
 * Хранить правило внутри объекта нужно, чтобы тесты задания 13 могли проверить
 * не только «отказ», но и то, какое именно правило сработало, а в логах
 * было видно, почему студенту отказали.
 * Зависимости: его создаёт EnrollmentService, его читают CourseHubFacade
 * (задание 15) и тесты задания 13.
 */
public final class EnrollmentDecision {

    private final boolean accepted;
    private final ErrorCode error;
    private final String rule;
    private final String comment;

    private EnrollmentDecision(boolean accepted, ErrorCode error, String rule, String comment) {
        this.accepted = accepted;
        this.error = error;
        this.rule = rule;
        this.comment = comment;
    }

    public static EnrollmentDecision accepted(String rule, String comment) {
        return new EnrollmentDecision(true, null, rule, comment);
    }

    public static EnrollmentDecision rejected(ErrorCode error, String rule, String comment) {
        return new EnrollmentDecision(false, error, rule, comment);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ErrorCode getError() {
        return error;
    }

    public String getRule() {
        return rule;
    }

    public String getComment() {
        return comment;
    }

    public String toString() {
        if (accepted) {
            return "ACCEPTED (" + rule + ")";
        }
        return "REJECTED " + error.code() + " (" + rule + ")";
    }
}

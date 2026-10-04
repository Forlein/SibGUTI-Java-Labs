package ru.sibguti.lab2.task13;

import ru.sibguti.lab2.Console;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.ErrorCode;

/*
 * Задание 13. Таблица решений.
 * Класс Task13DecisionTable строит полную таблицу зачисления: 4 статуса курса
 * на 2 варианта "есть свободные места" на 2 варианта "закрыт prerequisite"
 * на 2 варианта "повторная заявка" - всего 32 комбинации.
 * Каждая строка таблицы описана независимо от кода (правило R1..R5 и ожидаемый
 * код отказа), поэтому таблица может служить эталоном: тесты задания 13
 * сравнивают с ней реальное поведение EnrollmentService.
 * Из 32 комбинаций 7 считаются существенными, а 25 удаляются как дублирующие:
 * они не покрывают ни одного нового правила, потому что решающее условие
 * в них то же самое. Отметка об удалении есть прямо в таблице.
 * Зависимости: использует доменные CourseStatus и ErrorCode; саму проверку
 * правил делает EnrollmentService, а тесты задания 13 сверяют таблицу с ним.
 */
public class Task13DecisionTable {

    private static final String RULE_COURSE_NOT_OPEN = "R1";
    private static final String RULE_ALREADY_ENROLLED = "R2";
    private static final String RULE_NO_FREE_SEATS = "R3";
    private static final String RULE_PREREQUISITE_MISSING = "R4";
    private static final String RULE_ACCEPTED = "R5";

    /*
     * Вложенный класс Row - одна строка таблицы: условия, ожидаемое решение,
     * номер правила и отметка, оставлена ли строка как существенная.
     * Его создаёт buildAllRows, а читают main и тесты задания 13.
     */
    public static class Row {

        private final CourseStatus status;
        private final boolean freeSeats;
        private final boolean prerequisiteMet;
        private final boolean alreadyApplied;
        private final boolean accepted;
        private final ErrorCode expectedCode;
        private final String rule;
        private final boolean essential;
        private final String comment;

        private Row(CourseStatus status, boolean freeSeats, boolean prerequisiteMet, boolean alreadyApplied,
                    boolean accepted, ErrorCode expectedCode, String rule, boolean essential, String comment) {
            this.status = status;
            this.freeSeats = freeSeats;
            this.prerequisiteMet = prerequisiteMet;
            this.alreadyApplied = alreadyApplied;
            this.accepted = accepted;
            this.expectedCode = expectedCode;
            this.rule = rule;
            this.essential = essential;
            this.comment = comment;
        }

        public CourseStatus getStatus() {
            return status;
        }

        public boolean hasFreeSeats() {
            return freeSeats;
        }

        public boolean isPrerequisiteMet() {
            return prerequisiteMet;
        }

        public boolean isAlreadyApplied() {
            return alreadyApplied;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public ErrorCode getExpectedCode() {
            return expectedCode;
        }

        public String getRule() {
            return rule;
        }

        public boolean isEssential() {
            return essential;
        }

        public String getComment() {
            return comment;
        }
    }

    /*
     * Главный метод печатает все 32 строки таблицы с отметкой "существенная"
     * или "дубликат удалён", а затем итог: сколько строк оставлено и почему
     * остальные не дают нового покрытия. Его вызывает среда разработки.
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 13. ТАБЛИЦА РЕШЕНИЙ И ТЕСТЫ");
        System.out.println();

        Row[] rows = buildAllRows();
        System.out.println("Статус    Места  Prereq  Заявка | Правило | Ожидаемый исход      | Строка");
        System.out.println("----------+-------+-------+--------+--------+----------------------+-----------------");
        for (int i = 0; i < rows.length; i++) {
            Row row = rows[i];
            System.out.println(pad(row.getStatus().name(), 10) + "| "
                    + pad(String.valueOf(row.hasFreeSeats()), 6) + "| "
                    + pad(String.valueOf(row.isPrerequisiteMet()), 6) + "| "
                    + pad(String.valueOf(row.isAlreadyApplied()), 7) + "| "
                    + pad(row.getRule(), 7) + "| "
                    + pad(expectedText(row), 20) + "| "
                    + (row.isEssential() ? "СУЩЕСТВЕННАЯ" : "дубликат удалён"));
        }

        System.out.println();
        System.out.println("Итог: всего комбинаций " + rows.length
                + ", существенных " + countEssential(rows)
                + ", удалено дубликатов " + countDuplicates(rows));
        System.out.println("Правило R1 не зависит от мест, prerequisite и заявки, поэтому из 8 строк");
        System.out.println("с закрытым набором достаточно двух: одна где всё хорошо, и одна где всё плохо.");
        System.out.println("Правило R2 не зависит от мест и prerequisite - достаточно одной строки.");
        System.out.println("Правило R3 не зависит от prerequisite - достаточно одной строки.");
        System.out.println("Правила R4 и R5 уникальны и проверяются одной строкой каждое.");
    }

    /*
     * Метод buildAllRows строит все строки таблицы: по одному на каждую
     * комбинацию статуса, мест, prerequisite и заявки, то есть 4 * 2 * 2 * 2.
     * Размер массива считается от этих же величин, поэтому добавление
     * нового статуса не приведёт к падению метода.
     * Ожидания записаны здесь как обычные данные, а не вычислены через
     * EnrollmentService: иначе тест сравнивал бы код сам с собой
     * и ничего бы не проверял.
     * Его вызывает main, а читают тесты задания 13.
     */
    public static Row[] buildAllRows() {
        CourseStatus[] statuses = CourseStatus.values();
        boolean[] flags = {true, false};
        int combinations = statuses.length * flags.length * flags.length * flags.length;
        Row[] rows = new Row[combinations];
        int index = 0;

        for (int s = 0; s < statuses.length; s++) {
            for (int f = 0; f < flags.length; f++) {
                for (int p = 0; p < flags.length; p++) {
                    for (int a = 0; a < flags.length; a++) {
                        rows[index] = createRow(statuses[s], flags[f], flags[p], flags[a]);
                        index = index + 1;
                    }
                }
            }
        }
        return rows;
    }

    /*
     * Метод createRow делает одну строку таблицы: по условиям выбираются
     * правило, ожидаемый код отказа и признак существенности.
     * Его вызывает buildAllRows для каждой из 32 комбинаций.
     */
    private static Row createRow(CourseStatus status, boolean freeSeats, boolean prerequisiteMet,
                                 boolean alreadyApplied) {
        if (status != CourseStatus.OPEN) {
            boolean essential = isEssentialStatusRow(status, freeSeats, prerequisiteMet, alreadyApplied);
            return new Row(status, freeSeats, prerequisiteMet, alreadyApplied, false,
                    ErrorCode.COURSE_NOT_OPEN, RULE_COURSE_NOT_OPEN, essential,
                    "набор не открыт, остальные условия не смотрят");
        }

        if (alreadyApplied) {
            return new Row(status, freeSeats, prerequisiteMet, alreadyApplied, false,
                    ErrorCode.ALREADY_ENROLLED, RULE_ALREADY_ENROLLED,
                    freeSeats && prerequisiteMet,
                    "повторная заявка запрещена");
        }

        if (!freeSeats) {
            return new Row(status, freeSeats, prerequisiteMet, alreadyApplied, false,
                    ErrorCode.NO_FREE_SEATS, RULE_NO_FREE_SEATS,
                    prerequisiteMet,
                    "мест нет, prerequisite уже не смотрим");
        }

        if (!prerequisiteMet) {
            return new Row(status, freeSeats, prerequisiteMet, alreadyApplied, false,
                    ErrorCode.PREREQUISITE_MISSING, RULE_PREREQUISITE_MISSING, true,
                    "не закрыта предварительная дисциплина");
        }

        return new Row(status, freeSeats, prerequisiteMet, alreadyApplied, true,
                null, RULE_ACCEPTED, true, "все условия выполнены");
    }

    /*
     * Метод isEssentialStatusRow отмечает существенную строку среди статусов,
     * где зачисление запрещено всегда. Правило R1 не зависит ни от мест,
     * ни от prerequisite, ни от заявки, поэтому для каждого такого статуса
     * оставляется ровно одна строка - с идеальными остальными условиями,
     * кроме CANCELLED, где взята противоположная комбинация, чтобы в таблице
     * был виден случай "отказ даже при полной неудаче".
     * Его вызывает createRow.
     */
    private static boolean isEssentialStatusRow(CourseStatus status, boolean freeSeats,
                                                boolean prerequisiteMet, boolean alreadyApplied) {
        if (status == CourseStatus.CANCELLED) {
            return !freeSeats && !prerequisiteMet && alreadyApplied;
        }
        return freeSeats && prerequisiteMet && !alreadyApplied;
    }

    /*
     * Метод essentialRows возвращает только существенные строки таблицы.
     * Его вызывают тесты задания 13: на каждую такую строку пишется
     * отдельный тест, а дубликаты не тестируются вовсе.
     */
    public static Row[] essentialRows() {
        Row[] all = buildAllRows();
        int total = countEssential(all);
        Row[] result = new Row[total];
        int index = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].isEssential()) {
                result[index] = all[i];
                index = index + 1;
            }
        }
        return result;
    }

    /*
     * Метод countEssential считает количество существенных строк.
     * Его вызывают main и essentialRows.
     */
    public static int countEssential(Row[] rows) {
        int counter = 0;
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].isEssential()) {
                counter = counter + 1;
            }
        }
        return counter;
    }

    /*
     * Метод countDuplicates считает количество удалённых дубликатов.
     * Его вызывает main, чтобы показать экономию тестов.
     */
    public static int countDuplicates(Row[] rows) {
        return rows.length - countEssential(rows);
    }

    /*
     * Метод expectedText возвращает короткий текст ожидаемого исхода строки.
     * Его вызывает main при печати таблицы.
     */
    private static String expectedText(Row row) {
        if (row.isAccepted()) {
            return "ЗАЧИСЛИТЬ";
        }
        return row.getExpectedCode().code();
    }

    /*
     * Метод pad дополняет текст пробелами до заданной ширины, чтобы таблица
     * печаталась в колонках. Его вызывает главный метод при выводе строк.
     */
    private static String pad(String text, int width) {
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < width) {
            builder.append(' ');
        }
        return builder.toString();
    }
}

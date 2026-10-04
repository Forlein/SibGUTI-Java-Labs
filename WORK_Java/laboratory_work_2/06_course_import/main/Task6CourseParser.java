package ru.sibguti.lab2.task06;

/*
 * Задание 6. Граница перевода исключения (часть 2).
 * Класс Task6CourseParser - низкоуровневый разборщик одной записи вида
 * "CS101;Основы Java;72". Он ничего не знает про импорт и про номера записей,
 * поэтому на негодных данных он бросает обычный NumberFormatException
 * из Integer.parseInt. Именно этот класс ничего не ловит:
 * переводом ошибок занимается уровень выше.
 * Зависимости: его вызывает Task6ImportService, который ловит
 * NumberFormatException и превращает его в Task6CourseImportException.
 */
public class Task6CourseParser {

    public static final int FIELDS_COUNT = 3;

    public CourseRecord parse(String line) {
        String[] fields = splitLine(line);
        CourseCodeHolder code = new CourseCodeHolder(fields[0]);
        int hours = parseHours(fields[2]);
        return new CourseRecord(code.value, fields[1], hours);
    }

    private String[] splitLine(String line) {
        String[] fields = line.split(";");
        if (fields.length != FIELDS_COUNT) {
            throw new IllegalArgumentException(
                    "ожидалось " + FIELDS_COUNT + " поля, получено " + fields.length);
        }
        return fields;
    }

    private int parseHours(String rawHours) {
        return Integer.parseInt(rawHours.trim());
    }

    /*
     * Вложенный класс CourseRecord - разобранная запись: код, название и часы.
     * Его создаёт parse, а читает Task6ImportService при успешном импорте.
     */
    public static class CourseRecord {

        private final String code;
        private final String title;
        private final int hours;

        public CourseRecord(String code, String title, int hours) {
            this.code = code;
            this.title = title;
            this.hours = hours;
        }

        public String getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }

        public int getHours() {
            return hours;
        }

        public String toString() {
            return code + " / " + title + " / " + hours + " ч.";
        }
    }

    /*
     * Вложенный класс CourseCodeHolder - минимальная замена полноценному
     * объекту CourseCode из домена: в этом задании важна сама граница
     * перевода исключения, а не модель курса. Его создаёт parse.
     */
    public static class CourseCodeHolder {

        private final String value;

        public CourseCodeHolder(String value) {
            this.value = value.trim().toUpperCase();
        }

        public String value() {
            return value;
        }
    }
}

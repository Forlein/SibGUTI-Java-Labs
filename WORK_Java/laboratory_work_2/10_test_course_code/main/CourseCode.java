package ru.sibguti.lab2.domain;

import java.util.Locale;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 3, перенесён в задание 10 без изменений.
 * Зачем он нужен в этом задании: нормализует и проверяет код курса вида ББЧЧЧ.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс CourseCode — объект-значение для кода курса.
 * Код приводится к нормальному виду (без пробелов, дефисов и подчёркиваний,
 * в верхнем регистре) и после этого обязан иметь вид "ББЧЧЧ": две буквы
 * и три цифры, например "CS101". Нормализация нужна, чтобы "cs-101",
 * " cs101 " и "CS_101" считались одним и тем же курсом.
 * Зависимости: его создаёт Course (доменная модель), его нормализацию
 * проверяют параметризованные тесты задания 10, а неверный код на границе
 * импорта превращается в ErrorCode.INVALID_COURSE_CODE в задании 15.
 */
public final class CourseCode {

    public static final int LETTERS_COUNT = 2;
    public static final int DIGITS_COUNT = 3;
    public static final int EXPECTED_LENGTH = LETTERS_COUNT + DIGITS_COUNT;

    private final String value;

    private CourseCode(String value) {
        this.value = value;
    }

    public static CourseCode of(String rawCode) {
        String normalized = normalize(rawCode);
        validate(normalized);
        return new CourseCode(normalized);
    }

    public static String normalize(String rawCode) {
        if (rawCode == null) {
            throw new IllegalArgumentException("CourseCode: код курса не может быть null");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rawCode.length(); i++) {
            char symbol = rawCode.charAt(i);
            if (symbol == ' ' || symbol == '-' || symbol == '_' || symbol == '.') {
                continue;
            }
            builder.append(Character.toUpperCase(symbol));
        }
        return builder.toString().toUpperCase(Locale.ROOT);
    }

    private static void validate(String normalized) {
        if (normalized.length() != EXPECTED_LENGTH) {
            throw new IllegalArgumentException(
                    "CourseCode: ожидалось " + EXPECTED_LENGTH + " символа, получено " + normalized.length());
        }
        for (int i = 0; i < LETTERS_COUNT; i++) {
            if (!Character.isLetter(normalized.charAt(i))) {
                throw new IllegalArgumentException("CourseCode: первые " + LETTERS_COUNT + " символа должны быть буквами");
            }
        }
        for (int i = LETTERS_COUNT; i < EXPECTED_LENGTH; i++) {
            if (!Character.isDigit(normalized.charAt(i))) {
                throw new IllegalArgumentException("CourseCode: последние " + DIGITS_COUNT + " символа должны быть цифрами");
            }
        }
    }

    public String value() {
        return value;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CourseCode)) {
            return false;
        }
        return value.equals(((CourseCode) other).value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return value;
    }
}

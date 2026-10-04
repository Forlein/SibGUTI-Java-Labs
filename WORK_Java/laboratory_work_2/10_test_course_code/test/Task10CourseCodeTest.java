package ru.sibguti.lab2.task10;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sibguti.lab2.domain.CourseCode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Задание 10. Параметризованный тест.
 * Класс Task10CourseCodeTest проверяет нормализацию кодов курса и отказ
 * для недопустимых кодов. Одни и те же правила прогоняются на наборе значений
 * через @ParameterizedTest: данные вынесены из кода теста, поэтому добавить
 * новый случай - значит добавить строку, а не писать новый метод.
 * В имени каждого теста (@ParameterizedTest(name = ...)) показываются входные
 * данные и ожидаемый результат, поэтому в отчёте сразу видно, что сломалось.
 * Зависимости: использует доменный объект-значение CourseCode, который
 * нормализует код и бросает IllegalArgumentException на недопустимых данных.
 */
class Task10CourseCodeTest {

    /*
     * Нормализация приводит разные написания одного кода к одному виду:
     * убирает пробелы, дефисы и подчёркивания и переводит буквы в верхний регистр.
     * Первая колонка - то, что прислал пользователь, вторая - ожидаемый код.
     */
    @ParameterizedTest(name = "код \"{0}\" нормализуется в \"{1}\"")
    @CsvSource({
            "'cs101', CS101",
            "'CS101', CS101",
            "' cs-101 ', CS101",
            "'CS_101', CS101",
            "'C s - 1 0 1', CS101",
            "'cs.101', CS101"
    })
    @DisplayName("CourseCode нормализует допустимые коды")
    void validCodesAreNormalized(String rawCode, String expected) {
        CourseCode code = CourseCode.of(rawCode);

        assertEquals(expected, code.value(), "нормализованный код курса");
    }

    /*
     * Недопустимые коды проверяются вторым параметризованным тестом.
     * Данные подаются через @MethodSource: сюда удобно класть не только сам код,
     * но и текст ожидаемой причины, чтобы имя теста объясняло отказ.
     * Внутри lambda только одно действие - создание объекта, - поэтому
     * assertThrows проверяет ровно одну причину падения.
     */
    @ParameterizedTest(name = "код \"{0}\" отклоняется: {1}")
    @MethodSource("invalidCodes")
    @DisplayName("CourseCode отклоняет недопустимые коды")
    void invalidCodesAreRejected(String rawCode, String reason) {
        assertThrows(IllegalArgumentException.class, () -> CourseCode.of(rawCode), reason);
    }

    /*
     * Метод invalidCodes отдаёт набор недопустимых кодов вместе с причиной отказа.
     * Его вызывает аннотация @MethodSource у теста invalidCodesAreRejected.
     * Здесь нет ни данных, ни проверок - только подготовленные данные.
     */
    static List<Arguments> invalidCodes() {
        List<Arguments> cases = new ArrayList<Arguments>();
        cases.add(Arguments.of("CS10", "код слишком короткий"));
        cases.add(Arguments.of("CS1010", "код слишком длинный"));
        cases.add(Arguments.of("101", "в коде нет букв"));
        cases.add(Arguments.of("CSABC", "в коде нет цифр"));
        cases.add(Arguments.of("", "пустой код"));
        cases.add(Arguments.of("   ", "из пробелов ничего не осталось"));
        cases.add(Arguments.of(null, "код не может быть null"));
        return cases;
    }

    /*
     * Тест проверяет, что разные написания одного кода равны как объекты,
     * а не только печатаются одинаково. Это важно для поиска курса в каталоге:
     * сравнение строк здесь не годится, сравниваются значения.
     */
    @Test
    @DisplayName("нормализованные коды равны между собой")
    void normalizedCodesAreEqual() {
        CourseCode first = CourseCode.of("cs-101");
        CourseCode second = CourseCode.of(" CS_101 ");

        assertEquals(first, second, "коды cs-101 и CS_101 должны быть одним и тем же курсом");
    }

    /*
     * Тест фиксирует hashCode объект-значения: два одинаковых кода должны
     * попадать в одну корзину HashMap, иначе поиск курса будет работать неверно.
     */
    @Test
    @DisplayName("одинаковые коды имеют одинаковый hashCode")
    void equalCodesHaveEqualHashCodes() {
        assertEquals(CourseCode.of("cs101").hashCode(), CourseCode.of("CS-101").hashCode(),
                "hashCode одинаковых кодов должен совпадать");
    }
}

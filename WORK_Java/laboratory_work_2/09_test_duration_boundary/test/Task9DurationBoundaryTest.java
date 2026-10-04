package ru.sibguti.lab2.task09;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.sibguti.lab2.domain.Course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*
 * Задание 9. Граничные значения.
 * Класс Task9DurationBoundaryTest проверяет продолжительность курса с четырёх
 * сторон каждой границы: на единицу ниже минимума, ровно минимум, ровно максимум
 * и на единицу выше максимума. Контракт длительности лежит именно на границах,
 * поэтому проверять нужно их, а не "обычное" значение.
 * Почему одного обычного примера мало: тест с 72 часами проходит и при контракте
 * "1..1000", и при контракте "16..240", поэтому он не фиксирует ни одну границу.
 * Меняя MIN_DURATION_HOURS или MAX_DURATION_HOURS в проде, такой тест останется
 * зелёным, а вот тест на границах сразу покажет, что контракт нарушен.
 * Тесты параметризованные (@ParameterizedTest + @CsvSource), поэтому в отчёте видно
 * входное значение и ожидаемый исход для каждой строки отдельно.
 */
class Task9DurationBoundaryTest {

    /*
     * Параметризованный тест: первая колонка - входная длительность в часах,
     * вторая - ожидаемый исход ("принято" или "отказ"). Значения идут вплотную
     * к обеим границам: MIN_DURATION_HOURS - 1, MIN_DURATION_HOURS,
     * MAX_DURATION_HOURS и MAX_DURATION_HOURS + 1.
     */
    @ParameterizedTest(name = "длительность {0} ч. -> {1}")
    @CsvSource({
            "15, отказ",
            "16, принято",
            "240, принято",
            "241, отказ"
    })
    @DisplayName("длительность курса проверяется на обеих границах диапазона")
    void durationIsAcceptedOnlyInsideRange(int hours, String expected) {
        if ("отказ".equals(expected)) {
            assertThrows(IllegalArgumentException.class, () -> new Course("CS101", "Основы Java", hours, 30),
                    "длительность вне 16..240 часов должна отклоняться");
        } else {
            Course course = new Course("CS101", "Основы Java", hours, 30);

            assertEquals(hours, course.getDurationHours(),
                    "длительность внутри диапазона должна сохраняться без изменений");
        }
    }

    /*
     * Тест фиксирует сами границы диапазона как часть контракта: если их поменять,
     * тест упадёт и напомнит, что контракт изменился намеренно.
     */
    @Test
    @DisplayName("минимальная и максимальная длительность равны заявленным значениям")
    void boundariesArePartOfTheContract() {
        assertEquals(16, Course.MIN_DURATION_HOURS, "минимум длительности курса");
        assertEquals(240, Course.MAX_DURATION_HOURS, "максимум длительности курса");
    }
}

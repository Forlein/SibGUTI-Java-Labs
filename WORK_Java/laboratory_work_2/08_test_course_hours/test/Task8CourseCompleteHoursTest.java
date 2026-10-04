package ru.sibguti.lab2.task08;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sibguti.lab2.domain.Course;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Задание 8. Первый тест по AAA.
 * Класс Task8CourseCompleteHoursTest проверяет один успешный случай метода
 * Course.completeHours в стиле Arrange-Act-Assert: сначала создаётся объект
 * (Arrange), затем вызывается метод (Act), затем проверяется результат (Assert).
 * Имя теста осмысленное и читается как утверждение о поведении, а проверка
 * ровно одна, поэтому у теста не может быть нескольких разных причин падения.
 * JUnit Jupiter подключён в pom.xml: зависимость junit-jupiter и плагин
 * maven-surefire-plugin, который запускает тесты при команде mvn test.
 */
class Task8CourseCompleteHoursTest {

    /*
     * Тест проверяет, что completeHours закрывает ровно переданное число часов.
     * Arrange - создаём курс на 72 часа, Act - закрываем 12 часов,
     * Assert - ожидаем ровно 12 закрытых часов. Одна проверка - одна причина падения.
     */
    @Test
    @DisplayName("completeHours закрывает ровно переданное число часов")
    void completeHoursClosesExactlyGivenNumberOfHours() {
        Course course = new Course("CS101", "Основы Java", 72, 30);

        course.completeHours(12);

        assertEquals(12, course.getCompletedHours(),
                "после completeHours(12) закрыто должно быть ровно 12 часов");
    }
}

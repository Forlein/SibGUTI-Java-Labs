package ru.sibguti.lab2.task12;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sibguti.lab2.domain.Course;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 12. Устранение хрупких тестов.
 * Класс Task12FragileTestsFixedTest - это исправленные тесты: каждый из них
 * можно запустить отдельно и в любом порядке, и он всё равно пройдёт.
 * Четыре источника хрупкости и как они устранены:
 * 1) Порядок выполнения. Тесты не делят одну последовательность действий и не
 *    ждут результатов друг друга: каждый сам создаёт свои данные.
 * 2) Общее static state. Счётчик и каталог - обычные поля экземпляра,
 *    они заново создаются в @BeforeEach перед каждым тестом.
 * 3) Текущая директория. Файл читается из classpath через getResourceAsStream,
 *    а не как new File("data/courses.csv"), поэтому тест проходит при запуске
 *    из любой папки (из корня проекта, из папки target, из IDE).
 * 4) Точный текст toString. Проверяются поля объекта (код, длительность, часы),
 *    а от toString берётся только наличие кода внутри строки. Полное сравнение
 *    текста ломается при любом изменении формата, даже когда поведение не менялось.
 */
class Task12FragileTestsFixedTest {

    private List<String> catalog;
    private int requestCounter;

    /*
     * Метод createCleanState создаёт новые каталог и счётчик перед каждым тестом.
     * Его вызывает JUnit автоматически (аннотация @BeforeEach). Именно это
     * избавляет тесты от общего static state: ни один тест не видит данных
     * предыдущего, поэтому порядок запуска не имеет значения.
     */
    @BeforeEach
    void createCleanState() {
        catalog = new ArrayList<String>();
        requestCounter = 0;
    }

    /*
     * Тест проверяет, что счётчик начинается с нуля. Второй такой же тест
     * ниже проходит независимо от первого - это и есть проверка на порядок.
     */
    @Test
    @DisplayName("каждый тест начинается с нулевого счётчика (тест 1)")
    void firstTestSeesZeroCounter() {
        requestCounter = requestCounter + 1;

        assertEquals(1, requestCounter, "после одного обращения счётчик равен 1");
    }

    /*
     * Второй тест делает ровно то же действие и тоже ждёт единицу.
     * Если бы счётчик был static, один из двух тестов упал бы в зависимости
     * от того, какой из них выполнился первым.
     */
    @Test
    @DisplayName("каждый тест начинается с нулевого счётчика (тест 2)")
    void secondTestSeesZeroCounter() {
        requestCounter = requestCounter + 1;

        assertEquals(1, requestCounter, "после одного обращения счётчик равен 1");
    }

    /*
     * Тест проверяет, что каталог в начале теста пуст, даже если другой тест
     * уже добавлял в него курс до наполнения.
     */
    @Test
    @DisplayName("каталог в начале теста пуст независимо от порядка")
    void catalogIsEmptyAtTestStart() {
        catalog.add("CS101");

        assertEquals(1, catalog.size(), "в этом тесте каталог содержит только добавленный им курс");
    }

    /*
     * Тест проверяет то же самое другим способом: два теста, каждый из которых
     * добавляет по одному курсу, не влияют на итоговое число элементов,
     * потому что данные не разделяются.
     */
    @Test
    @DisplayName("свои данные каждого теста не смешиваются с чужими")
    void ownCatalogIsNotShared() {
        catalog.add("CS101");
        catalog.add("CS102");

        assertEquals(2, catalog.size(), "в этом тесте только два курса, добавленные самим тестом");
    }

    /*
     * Тест читает csv из classpath и проверяет содержимое. Путь берётся
     * у загрузчика классов, а не у файловой системы, поэтому результат
     * не зависит от того, из какой директории запущены тесты.
     */
    @Test
    @DisplayName("файл читается из classpath, а не из текущей директории")
    void readsDataFileFromClasspath() throws IOException {
        InputStream stream = getClass().getResourceAsStream("/courses-ok.csv");
        assertNotNull(stream, "ресурс courses-ok.csv должен лежать в classpath");

        List<String> lines = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        final List<String> importedLines = lines;

        assertAll(
                () -> assertEquals(3, importedLines.size(), "в файле три записи курсов"),
                () -> assertTrue(importedLines.get(0).startsWith("CS101;"), "первая запись файла - CS101")
        );
    }

    /*
     * Тест проверяет поля курса вместо полного текста toString.
     * Он переживёт и переименование поля в toString, и изменение формата,
     * потому что описывает поведение, а не оформление вывода.
     */
    @Test
    @DisplayName("проверяются поля объекта, а не полный текст toString")
    void assertsFieldsInsteadOfWholeToString() {
        Course course = new Course("CS101", "Основы Java", 72, 30);

        assertAll(
                () -> assertEquals("CS101", course.getCode().value(), "код курса"),
                () -> assertEquals("Основы Java", course.getTitle(), "название курса"),
                () -> assertEquals(72, course.getDurationHours(), "длительность курса"),
                () -> assertEquals(0, course.getCompletedHours(), "изначально закрыто ноль часов"),
                () -> assertTrue(course.toString().contains("CS101"), "toString должен содержать код курса")
        );
    }
}

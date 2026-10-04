package ru.sibguti.lab2.task14;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.hub.InMemoryCourseRepository;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 14. Ресурс с двумя отказами (тесты).
 * Класс Task14CourseImporterTest проверяет импортёр из абстрактного Reader
 * в самой сложной ситуации: когда сбой происходит и при чтении записи,
 * и при закрытии ресурса. Проверяются четыре факта: основное исключение,
 * suppressed-ошибка закрытия, число успевших разобранных записей и то, что
 * в хранилище не попало ничего (частичной публикации не бывает).
 * Зависимости: использует CourseImporter и ImportFailedException из этого
 * пакета и InMemoryCourseRepository из пакета hub.
 */
class Task14CourseImporterTest {

    private static final String GOOD_DATA =
            "CS101;Основы Java;72;30;-\n"
                    + "CS102;Базы данных;80;25;CS101\n";

    private InMemoryCourseRepository repository;
    private CourseImporter importer;

    /*
     * Метод createFixture создаёт новое хранилище и новый импортёр перед каждым
     * тестом, поэтому тесты не зависят друг от друга и могут идти в любом порядке.
     * Его вызывает JUnit автоматически по аннотации @BeforeEach.
     */
    @BeforeEach
    void createFixture() {
        repository = new InMemoryCourseRepository();
        importer = new CourseImporter(repository);
    }

    /*
     * Тест успешного импорта: обе записи разобраны и опубликованы целиком.
     */
    @Test
    @DisplayName("корректные данные импортируются и публикуются целиком")
    void goodDataIsImportedFully() throws ImportFailedException {
        ImportReport report = importer.importFrom(new StringReader(GOOD_DATA));

        assertAll(
                () -> assertEquals(2, report.getRecordsImported(), "разобрано обе записи"),
                () -> assertEquals(2, repository.size(), "в хранилище попали обе записи")
        );
    }

    /*
     * Тест главной ситуации задания: вторая запись повреждена, а Reader
     * дополнительно падает при закрытии. Основным считается отказ разбора,
     * ошибка закрытия попадает в suppressed и не теряется.
     * В assertThrows попадает одно действие - вызов importFrom.
     */
    @Test
    @DisplayName("ошибка разбора становится основной, ошибка закрытия уходит в suppressed")
    void malformedRecordAndCloseErrorAreBothReported() {
        String data = "CS101;Основы Java;72;30;-\n" + "CS102;Базы данных;мусор;25;-\n";
        Reader reader = new FailingOnCloseReader(data);

        ImportFailedException exception = assertThrows(ImportFailedException.class,
                () -> importer.importFrom(reader),
                "повреждённая запись должна прерывать импорт");

        assertAll(
                () -> assertEquals(ErrorCode.MALFORMED_RECORD, exception.getErrorCode(),
                        "основной код отказа"),
                () -> assertEquals(2, exception.getRecordNumber(),
                        "номер повреждённой записи (вторая по порядку, первая разобрана)"),
                () -> assertEquals(1, exception.getRecordsProcessed(),
                        "до отказа успешно разобрана одна запись"),
                () -> assertNotNull(exception.getCause(), "исходная причина разбора сохранена"),
                () -> assertEquals(NumberFormatException.class, exception.getCause().getClass(),
                        "исходная причина - NumberFormatException из разбора чисел"),
                () -> assertEquals(1, exception.getSuppressed().length,
                        "ошибка закрытия должна попасть в suppressed"),
                () -> assertEquals(0, repository.size(),
                        "частично опубликованного результата быть не должно")
        );
    }

    /*
     * Тест проверяет, что ошибка закрытия не теряется и тогда, когда разбор
     * прошёл полностью: импорт считается неудачным и публикации не происходит.
     */
    @Test
    @DisplayName("ошибка закрытия после успешного чтения тоже прерывает импорт")
    void closeErrorAfterSuccessfulReadFailsImport() {
        Reader reader = new FailingOnCloseReader(GOOD_DATA);

        ImportFailedException exception = assertThrows(ImportFailedException.class,
                () -> importer.importFrom(reader),
                "незакрытый ресурс означает незавершённый импорт");

        assertAll(
                () -> assertEquals(ErrorCode.IMPORT_SOURCE_UNREADABLE, exception.getErrorCode(),
                        "код отказа источника"),
                () -> assertEquals(2, exception.getRecordsProcessed(),
                        "записи были разобраны, но не опубликованы"),
                () -> assertNotNull(exception.getCause(), "IOException от закрытия сохранена в cause"),
                () -> assertEquals(0, repository.size(), "публикации не было")
        );
    }

    /*
     * Тест проверяет, что при недоступном хранилище импорт прерывается,
     * а разобранные записи не публикуются.
     */
    @Test
    @DisplayName("недоступность хранилища прерывает импорт до публикации")
    void unavailableRepositoryFailsImport() {
        repository.setUnavailable(true);
        Reader reader = new StringReader(GOOD_DATA);

        ImportFailedException exception = assertThrows(ImportFailedException.class,
                () -> importer.importFrom(reader),
                "недоступное хранилище должно прервать импорт");

        assertAll(
                () -> assertEquals(ErrorCode.SERVICE_UNAVAILABLE, exception.getErrorCode(),
                        "код инфраструктурного отказа"),
                () -> assertEquals(2, exception.getRecordsProcessed(), "записи успели разобрать"),
                () -> assertEquals(0, repository.size(), "в хранилище ничего не попало")
        );
    }

    /*
     * Тест проверяет, что номер записи в отказе совпадает с номером плохой строки
     * в файле, а не с номером строки исходного текста.
     */
    @Test
    @DisplayName("в отказе указан номер плохой записи, а не номер строки файла")
    void recordNumberPointsToBrokenRecord() {
        String data = "CS201;Компиляторы;72;30;-\n"
                + "\n"
                + "CS202;Сети;не_число;25;-\n";

        ImportFailedException exception = assertThrows(ImportFailedException.class,
                () -> importer.importFrom(new StringReader(data)),
                "вторая непустая запись повреждена");

        assertAll(
                () -> assertEquals(2, exception.getRecordNumber(),
                        "пустая строка не должна считаться записью"),
                () -> assertEquals(1, exception.getRecordsProcessed(),
                        "до отказа разобрана одна запись"),
                () -> assertTrue(exception.getMessage().contains("E109"),
                        "сообщение должно содержать стабильный код MALFORMED_RECORD")
        );
    }

    /*
     * Вложенный класс FailingOnCloseReader - тестовый Reader, который читает
     * обычные данные, но всегда падает при close(). Он нужен, чтобы сымитировать
     * "ошибку закрытия" из задания и проверить suppressed-исключения.
     */
    private static final class FailingOnCloseReader extends Reader {

        private final StringReader delegate;

        FailingOnCloseReader(String data) {
            this.delegate = new StringReader(data);
        }

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            return delegate.read(buffer, offset, length);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("close() завершился ошибкой ввода-вывода");
        }
    }
}

package ru.sibguti.lab2.task15;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.hub.CourseHubFacade;
import ru.sibguti.lab2.hub.EnrollmentBook;
import ru.sibguti.lab2.hub.HubResult;
import ru.sibguti.lab2.hub.InMemoryCourseRepository;
import ru.sibguti.lab2.hub.RepositoryUnavailableException;
import ru.sibguti.lab2.task14.ImportFailedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 15. Полная граница ошибок CourseHub (тесты импорта, часть 1).
 * Класс Task15ImportBoundaryTest проверяет use case импорта на всех четырёх
 * видах отказа: ошибка данных, недоступный источник, недоступное хранилище
 * и сбой закрытия ресурса. Для каждого отказа проверяется, что публичный слой
 * вернул стабильный код ErrorCode, сохранил исходную причину в cause и
 * не опубликовал частичный результат.
 * Все тесты используют classpath-ресурсы вместо файлов по текущей директории,
 * поэтому они не зависят ни от порядка запуска, ни от места запуска.
 * Зависимости: использует публичный слой CourseHubFacade и хранилище
 * InMemoryCourseRepository, которые создаёт заново перед каждым тестом.
 */
class Task15ImportBoundaryTest {

    private InMemoryCourseRepository repository;
    private CourseHubFacade hub;

    /*
     * Метод createFixture создаёт новое хранилище, новый журнал и новый публичный
     * слой перед каждым тестом. Его вызывает JUnit по аннотации @BeforeEach.
     * Никаких общих static-полей здесь нет, поэтому порядок тестов не важен.
     */
    @BeforeEach
    void createFixture() {
        repository = new InMemoryCourseRepository();
        hub = new CourseHubFacade(repository, new EnrollmentBook());
    }

    /*
     * Тест успешного сценария: корректный файл целиком импортируется,
     * наружу возвращается успех без кода ошибки.
     */
    @Test
    @DisplayName("успешный импорт возвращает успех без кода ошибки")
    void goodFileIsImportedSuccessfully() {
        HubResult result = hub.importCourses(openResource("/courses-ok.csv"));

        assertAll(
                () -> assertTrue(result.isSuccess(), "импорт корректного файла должен быть успешным"),
                () -> assertNull(result.getErrorCode(), "успешный результат не содержит кода ошибки"),
                () -> assertEquals(3, repository.size(), "все три записи должны попасть в каталог")
        );
    }

    /*
     * Тест пустого источника: ноль записей - это успех, а не ошибка.
     * Иначе пустой файл ломал бы регулярный запуск импорта.
     */
    @Test
    @DisplayName("пустой источник даёт успех с нулём записей")
    void emptySourceIsSuccessfulWithZeroRecords() {
        HubResult result = hub.importCourses(new StringReader(""));

        assertAll(
                () -> assertTrue(result.isSuccess(), "пустой источник не является ошибкой"),
                () -> assertEquals(0, repository.size(), "в каталог ничего не попало")
        );
    }

    /*
     * Тест файла из одних пустых строк: они не считаются записями,
     * импорт остаётся успешным и пустым.
     */
    @Test
    @DisplayName("файл из пустых строк не считается записями")
    void blankLinesAreNotRecords() {
        HubResult result = hub.importCourses(new StringReader("\n   \n\n"));

        assertAll(
                () -> assertTrue(result.isSuccess(), "пустые строки не делают импорт неуспешным"),
                () -> assertEquals(0, repository.size(), "пустые строки не публикуются")
        );
    }

    /*
     * Тест ошибки данных: повреждённая запись даёт стабильный код E109,
     * исходная NumberFormatException остаётся в cause, а каталог пуст.
     */
    @Test
    @DisplayName("повреждённая запись даёт MALFORMED_RECORD и не публикует ничего")
    void malformedRecordReturnsStableCodeAndPublishesNothing() {
        HubResult result = hub.importCourses(openResource("/courses-broken.csv"));

        assertAll(
                () -> assertFalse(result.isSuccess(), "импорт с повреждённой записью неуспешен"),
                () -> assertEquals(ErrorCode.MALFORMED_RECORD, result.getErrorCode(), "код ошибки данных"),
                () -> assertEquals(0, repository.size(), "частичной публикации быть не должно"),
                () -> assertNotNull(result.getCause(), "исходная причина должна сохраниться в cause"),
                () -> assertEquals(ImportFailedException.class, result.getCause().getClass(),
                        "cause - это доменное исключение импорта"),
                () -> assertEquals(NumberFormatException.class, result.getCause().getCause().getClass(),
                        "в цепочке cause остаётся исходная NumberFormatException")
        );
    }

    /*
     * Тест недоступного источника: Reader падает при чтении,
     * наружу возвращается код IMPORT_SOURCE_UNREADABLE.
     */
    @Test
    @DisplayName("ошибка чтения источника даёт IMPORT_SOURCE_UNREADABLE")
    void unreadableSourceReturnsStableCode() {
        HubResult result = hub.importCourses(new FailingOnReadReader());

        assertAll(
                () -> assertFalse(result.isSuccess(), "нечитаемый источник означает неуспех"),
                () -> assertEquals(ErrorCode.IMPORT_SOURCE_UNREADABLE, result.getErrorCode(),
                        "код ошибки источника"),
                () -> assertNotNull(result.getCause().getCause(), "IOException сохраняется в цепочке cause")
        );
    }

    /*
     * Тест сбоя закрытия после успешного чтения: импорт считается незавершённым,
     * хотя все записи разобраны, и публикации не происходит.
     */
    @Test
    @DisplayName("сбой закрытия источника прерывает импорт до публикации")
    void closeFailureStopsImportBeforePublication() {
        Reader reader = new FailingOnCloseReader("CS101;Основы Java;72;30;-\n");

        HubResult result = hub.importCourses(reader);

        assertAll(
                () -> assertFalse(result.isSuccess(), "незакрытый источник означает незавершённый импорт"),
                () -> assertEquals(ErrorCode.IMPORT_SOURCE_UNREADABLE, result.getErrorCode(),
                        "код ошибки источника"),
                () -> assertEquals(0, repository.size(), "ни одна запись не опубликована")
        );
    }

    /*
     * Тест двойного отказа: сбой в записи плюс сбой закрытия.
     * Основным считается отказ разбора, ошибка закрытия не теряется -
     * она лежит в suppressed основного исключения внутри cause результата.
     */
    @Test
    @DisplayName("при сбое записи и сбое закрытия теряется ни одна из ошибок")
    void bothFailuresAreKept() {
        Reader reader = new FailingOnCloseReader("CS101;Основы Java;72;30;-\nCS102;Сети;мусор;25;-\n");

        HubResult result = hub.importCourses(reader);

        Throwable cause = result.getCause();
        assertAll(
                () -> assertEquals(ErrorCode.MALFORMED_RECORD, result.getErrorCode(), "основной код отказа"),
                () -> assertEquals(1, cause.getSuppressed().length,
                        "ошибка закрытия должна быть в suppressed"),
                () -> assertEquals(0, repository.size(), "публикации не было")
        );
    }

    /*
     * Тест инфраструктурного отказа: хранилище недоступно, наружу возвращается
     * код SERVICE_UNAVAILABLE, а RepositoryUnavailableException сохраняется
     * в цепочке cause для журнала.
     */
    @Test
    @DisplayName("недоступное хранилище даёт SERVICE_UNAVAILABLE с сохранённой причиной")
    void unavailableRepositoryReturnsServiceUnavailable() {
        repository.setUnavailable(true);

        HubResult result = hub.importCourses(openResource("/courses-ok.csv"));

        assertAll(
                () -> assertEquals(ErrorCode.SERVICE_UNAVAILABLE, result.getErrorCode(),
                        "код инфраструктурного отказа"),
                () -> assertEquals(RepositoryUnavailableException.class,
                        result.getCause().getCause().getClass(),
                        "исходная инфраструктурная ошибка сохраняется в цепочке cause"),
                () -> assertEquals(0, repository.size(), "в хранилище ничего не попало")
        );
    }

    /*
     * Негативный параметризованный тест: разные виды испорченных записей
     * обязаны давать один и тот же стабильный код MALFORMED_RECORD, иначе
     * клиенту придётся разбирать текст сообщений вместо кодов.
     * Данные берёт метод brokenRecords по аннотации @MethodSource.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("brokenRecords")
    @DisplayName("любая испорченная запись даёт один стабильный код MALFORMED_RECORD")
    void everyBrokenRecordGivesSameStableCode(String description, String line, int expectedRecordNumber) {
        HubResult result = hub.importCourses(new StringReader(line));

        assertAll(
                () -> assertEquals(ErrorCode.MALFORMED_RECORD, result.getErrorCode(), description),
                () -> assertEquals(expectedRecordNumber,
                        ((ru.sibguti.lab2.task14.ImportFailedException) result.getCause()).getRecordNumber(),
                        description + ": номер записи в отказе")
        );
    }

    /*
     * Метод brokenRecords готовит список испорченных записей: в каждой строке
     * записан свой вид поломки. Его вызывает аннотация @MethodSource.
     * Каждая запись состоит из двух строк файла, где первая повреждена,
     * поэтому номер отказа равен 1, а не 2.
     */
    static List<Arguments> brokenRecords() {
        List<Arguments> cases =
                new ArrayList<Arguments>();
        cases.add(Arguments.of(
                "не число в длительности", "CS301;Сети;много;25;-\nCS302;ОС;64;20;-\n", 1));
        cases.add(Arguments.of(
                "не число во вместимости", "CS303;Физтех;72;много;-\nCS304;Логика;64;20;-\n", 1));
        cases.add(Arguments.of(
                "не хватает полей", "CS305;Математика;72\nCS306;Геометрия;64;20;-\n", 1));
        cases.add(Arguments.of(
                "неверный код курса", "10;Математика;72;20;-\nCS307;Геометрия;64;20;-\n", 1));
        cases.add(Arguments.of(
                "длительность вне диапазона", "CS308;Математика;4;20;-\nCS309;Геометрия;64;20;-\n", 1));
        return cases;
    }

    /*
     * Тест проверяет, что сообщение результата не содержит внутренних деталей,
     * а причина остаётся доступной только через cause.
     */
    @Test
    @DisplayName("сообщение результата не раскрывает внутренние детали хранилища")
    void resultMessageDoesNotLeakInternalDetails() {
        repository.setUnavailable(true);

        HubResult result = hub.importCourses(openResource("/courses-ok.csv"));

        assertAll(
                () -> assertFalse(result.getMessage().contains("catalog-storage"),
                        "внутренние имена сервисов не должны попадать в сообщение"),
                () -> assertTrue(result.getCause() != null,
                        "причина отказа должна оставаться в cause для журнала")
        );
    }

    /*
     * Метод openResource открывает файл из classpath. Его вызывают тесты,
     * которым нужен готовый источник данных: путь берётся у загрузчика классов,
     * поэтому тест не зависит от текущей директории.
     */
    private Reader openResource(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("ресурс не найден: " + path);
        }
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }

    /*
     * Вложенный класс FailingOnReadReader - источник, который падает при чтении,
     * но закрывается нормально. Он имитирует недоступность файла в сети.
     */
    private static final class FailingOnReadReader extends Reader {

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            throw new IOException("чтение источника не удалось");
        }

        @Override
        public void close() {
        }
    }

    /*
     * Вложенный класс FailingOnCloseReader - источник, который читается нормально,
     * но всегда падает при закрытии. Он имитирует сбой закрытия файла.
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
            throw new IOException("закрытие источника не удалось");
        }
    }
}

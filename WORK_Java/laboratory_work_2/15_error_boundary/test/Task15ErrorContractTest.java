package ru.sibguti.lab2.task15;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.hub.CourseHubFacade;
import ru.sibguti.lab2.hub.CourseRepository;
import ru.sibguti.lab2.hub.EnrollmentBook;
import ru.sibguti.lab2.hub.HubResult;
import ru.sibguti.lab2.hub.InMemoryCourseRepository;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 15. Полная граница ошибок CourseHub (контракт, часть 3).
 * Класс Task15ErrorContractTest проверяет сам публичный контракт:
 *  1) все коды ErrorCode пригодны для передачи клиенту (не пустые, уникальные,
 *     с префиксом E и описанием), потому что на них строятся проверки в UI и логах;
 *  2) наружу выходит ровно один тип результата - HubResult, а программные
 *     ошибки (RuntimeException, Error) не маскируются под отказ бизнес-логики;
 *  3) цепочка cause сохраняется целиком, чтобы журнал мог разобрать отказ;
 *  4) тесты изолированы: каждый создаёт своё хранилище и журнал.
 * Именно эти пункты объясняют, почему глобальный catch (Exception) запрещён.
 * Зависимости: использует публичный слой CourseHubFacade, хранилища
 * InMemoryCourseRepository и CourseRepository, доменные ErrorCode и Course.
 */
class Task15ErrorContractTest {

    private InMemoryCourseRepository repository;
    private EnrollmentBook book;
    private CourseHubFacade hub;

    /*
     * Метод createFixture создаёт отдельные хранилище, журнал и публичный слой
     * перед каждым тестом. Именно это делает тесты независимыми: общих
     * статических данных в задании 15 нет. Его вызывает JUnit по @BeforeEach.
     */
    @BeforeEach
    void createFixture() {
        repository = new InMemoryCourseRepository();
        book = new EnrollmentBook();
        hub = new CourseHubFacade(repository, book);
    }

    /*
     * Параметризованный тест кода ошибки: у каждого элемента ErrorCode есть
     * непустой код с префиксом E и непустое описание. Данные берёт метод
     * allErrorCodes по аннотации @MethodSource.
     */
    @ParameterizedTest(name = "{0} -> код {1}")
    @MethodSource("allErrorCodes")
    @DisplayName("каждый код ошибки пригоден для передачи клиенту")
    void everyErrorCodeIsUsable(ErrorCode errorCode, String expectedCode) {
        assertAll(
                () -> assertEquals(expectedCode, errorCode.code(), "код не должен меняться"),
                () -> assertTrue(errorCode.code().startsWith("E"), "код начинается с E"),
                () -> assertTrue(errorCode.description().trim().isEmpty() == false,
                        "у кода должно быть описание")
        );
    }

    /*
     * Тест уникальности кодов: если два отказа поделят один код, клиент
     * не сможет их различить и придётся разбирать текст сообщения.
     */
    @Test
    @DisplayName("коды ошибок уникальны")
    void errorCodesAreUnique() {
        Set<String> codes = new HashSet<String>();
        List<String> duplicates = new ArrayList<String>();
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (codes.add(errorCode.code()) == false) {
                duplicates.add(errorCode.code());
            }
        }

        assertAll(
                () -> assertTrue(duplicates.isEmpty(), "повторяющиеся коды: " + duplicates),
                () -> assertEquals(ErrorCode.values().length, codes.size(),
                        "столько кодов, сколько и элементов перечисления")
        );
    }

    /*
     * Тест успешного результата: у успеха нет ни кода ошибки, ни причины,
     * поэтому клиент может проверять только признак isSuccess.
     */
    @Test
    @DisplayName("успешный результат не содержит кода ошибки и причины")
    void successfulResultHasNoErrorCodeAndNoCause() {
        HubResult result = hub.importCourses(new StringReader("CS101;Основы Java;72;30;-\n"));

        assertAll(
                () -> assertTrue(result.isSuccess(), "корректная запись импортируется"),
                () -> assertNull(result.getErrorCode(), "кода ошибки у успеха нет"),
                () -> assertNull(result.getCause(), "причины у успеха нет"),
                () -> assertTrue(result.getMessage().isEmpty() == false, "успех несёт понятное сообщение")
        );
    }

    /*
     * Тест цепочки причин при ошибке данных: публичный слой отдаёт
     * ImportFailedException, а внутри него остаётся исходная
     * NumberFormatException с номером разбиравшейся записи.
     */
    @Test
    @DisplayName("цепочка cause сохраняет исходную ошибку разбора")
    void causeChainKeepsOriginalParsingFailure() {
        HubResult result = hub.importCourses(new StringReader("CS101;Основы Java;много;30;-\n"));

        Throwable cause = result.getCause();
        assertAll(
                () -> assertEquals(ErrorCode.MALFORMED_RECORD, result.getErrorCode(), "код отказа данных"),
                () -> assertEquals(NumberFormatException.class, cause.getCause().getClass(),
                        "в цепочке осталась исходная причина разбора"),
                () -> assertEquals(ErrorCode.MALFORMED_RECORD,
                        ((ru.sibguti.lab2.task14.ImportFailedException) cause).getErrorCode(),
                        "код отказа сохранён в исключении импорта")
        );
    }

    /*
     * Тест программной ошибки: если хранилище бросает IllegalStateException,
     * публичный слой не превращает его в код отказа. Такой отказ - дефект
     * программы, и он обязан падать наружу громко, а не маскироваться
     * под внешнюю недоступность сервиса.
     */
    @Test
    @DisplayName("программная ошибка хранилища не маскируется под отказ сервиса")
    void programmingErrorIsNotMaskedAsServiceFailure() {
        CourseHubFacade brokenHub = new CourseHubFacade(new IllegalStateRepository(), new EnrollmentBook());

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> brokenHub.enroll("S-01", "CS101"));

        assertEquals("дефект хранилища", failure.getMessage(), "дефект должен дойти наружу без изменений");
    }

    /*
     * Тест ошибки из источника данных: RuntimeException от источника тоже
     * не скрывается под MALFORMED_RECORD, иначе чинить пришлось бы данные
     * вместо кода, который этот RuntimeException бросает.
     */
    @Test
    @DisplayName("RuntimeException источника не превращается в MALFORMED_RECORD")
    void runtimeFailureOfSourceIsNotHidden() {
        IllegalStateException sourceFailure = new IllegalStateException("ошибка драйвера источника");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> hub.importCourses(new FailingReader(sourceFailure)));

        assertAll(
                () -> assertSame(sourceFailure, thrown, "наружу выходит тот же объект исключения"),
                () -> assertEquals(0, repository.size(), "испорченный импорт ничего не публикует")
        );
    }

    /*
     * Тест фатальной ошибки: Error (например, OutOfMemoryError) не ловится
     * ни одним catch, поэтому глобальный catch (Exception) всё равно не даёт
     * гарантии "ничего наружу не выйдет". Этот тест фиксирует поведение слоя.
     */
    @Test
    @DisplayName("Error из источника не перехватывается слоем")
    void fatalErrorFromSourceIsNotCaught() {
        assertThrows(OutOfMemoryError.class, () -> hub.importCourses(new FailingReader(new OutOfMemoryError())));
    }

    /*
     * Тест отказа закрытия источника: он тоже считается незавершённым импортом,
     * потому что публикация не должна происходить, пока ресурс не закрыт.
     */
    @Test
    @DisplayName("IOException при закрытии источника даёт IMPORT_SOURCE_UNREADABLE")
    void closeFailureIsReportedAsSourceFailure() {
        HubResult result = hub.importCourses(new CloseFailingReader("CS101;Основы Java;72;30;-\n"));

        assertAll(
                () -> assertEquals(ErrorCode.IMPORT_SOURCE_UNREADABLE, result.getErrorCode(),
                        "сбой закрытия - это отказ источника"),
                () -> assertNotNull(result.getCause(), "причина сохранена"),
                () -> assertEquals(0, repository.size(), "запись не опубликована")
        );
    }

    /*
     * Тест изоляции: два публичных слоя, созданные в одном тесте, не делят данные.
     * Так проверяется требование задания: каждый сценарий получает своё хранилище.
     */
    @Test
    @DisplayName("хранилища разных публичных слоёв не влияют друг на друга")
    void storagesAreIsolated() {
        InMemoryCourseRepository otherRepository = new InMemoryCourseRepository();
        CourseHubFacade otherHub = new CourseHubFacade(otherRepository, new EnrollmentBook());
        hub.importCourses(new StringReader("CS101;Основы Java;72;30;-\n"));

        HubResult result = otherHub.importCourses(new StringReader("CS201;Сети;64;40;-\n"));

        assertAll(
                () -> assertTrue(result.isSuccess(), "второй импорт успешен"),
                () -> assertEquals(1, repository.size(), "в первом хранилище своя запись"),
                () -> assertEquals(1, otherRepository.size(), "во втором хранилище своя запись"),
                () -> assertEquals("CS101", repository.findByCode("CS101").get().getCode().value(),
                        "первое хранилище не затронуто"),
                () -> assertEquals("CS201", otherRepository.findByCode("CS201").get().getCode().value(),
                        "второе хранилище содержит свои данные")
        );
    }

    /*
     * Тест повторного импорта одного и того же файла: повторная загрузка тех же
     * курсов допустима, каталог просто дополняется второй копией - проверяем,
     * что публичный слой не падает и не теряет данные.
     */
    @Test
    @DisplayName("повторный импорт дополняет каталог и не падает")
    void repeatedImportIsSafe() {
        String data = "CS101;Основы Java;72;30;-\n";
        hub.importCourses(new StringReader(data));

        HubResult secondResult = hub.importCourses(new StringReader(data));

        assertAll(
                () -> assertTrue(secondResult.isSuccess(), "повторный импорт успешен"),
                () -> assertEquals(2, repository.size(), "обе записи в каталоге")
        );
    }

    /*
     * Метод allErrorCodes отдаёт набор всех элементов перечисления ErrorCode
     * вместе с ожидаемыми строковыми кодами. Его вызывает аннотация
     * @MethodSource у параметризованного теста кодов.
     */
    static List<org.junit.jupiter.params.provider.Arguments> allErrorCodes() {
        List<org.junit.jupiter.params.provider.Arguments> cases =
                new ArrayList<org.junit.jupiter.params.provider.Arguments>();
        ErrorCode[] expected = {
                ErrorCode.INVALID_COURSE_CODE,
                ErrorCode.COURSE_NOT_FOUND,
                ErrorCode.COURSE_NOT_OPEN,
                ErrorCode.NO_FREE_SEATS,
                ErrorCode.PREREQUISITE_MISSING,
                ErrorCode.ALREADY_ENROLLED,
                ErrorCode.HOURS_OUT_OF_RANGE,
                ErrorCode.MALFORMED_RECORD,
                ErrorCode.IMPORT_SOURCE_UNREADABLE,
                ErrorCode.IMPORT_INCOMPLETE,
                ErrorCode.SERVICE_UNAVAILABLE,
                ErrorCode.INTERNAL_ERROR
        };
        String[] codes = {"E101", "E102", "E103", "E104", "E105", "E106",
                "E108", "E109", "E110", "E111", "E500", "E900"};
        for (int i = 0; i < expected.length; i++) {
            cases.add(org.junit.jupiter.params.provider.Arguments.of(expected[i], codes[i]));
        }
        return cases;
    }

    /*
     * Вложенный класс IllegalStateRepository - хранилище, которое бросает
     * программную ошибку вместо объявленного отказа. Оно нужно тесту выше,
     * чтобы проверить, что публичный слой не превращает дефект в код ошибки.
     */
    private static final class IllegalStateRepository implements CourseRepository {

        public Optional<Course> findByCode(String normalizedCode) {
            throw new IllegalStateException("дефект хранилища");
        }

        public void saveAll(List<Course> courses) {
            throw new IllegalStateException("дефект хранилища");
        }

        public int size() {
            return 0;
        }
    }

    /*
     * Вложенный класс FailingReader - источник, который бросает заданное
     * непроверяемое исключение при первом же чтении. Его используют тесты
     * на RuntimeException и на Error.
     */
    private static final class FailingReader extends Reader {

        private final Throwable failure;

        FailingReader(Throwable failure) {
            this.failure = failure;
        }

        @Override
        public int read(char[] buffer, int offset, int length) {
            if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            throw (Error) failure;
        }

        @Override
        public void close() {
        }
    }

    /*
     * Вложенный класс CloseFailingReader - источник с нормальными данными,
     * который всегда падает при закрытии. Он имитирует сбой закрытия файла
     * после полностью успешного чтения.
     */
    private static final class CloseFailingReader extends Reader {

        private final StringReader delegate;

        CloseFailingReader(String data) {
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
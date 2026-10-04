package ru.sibguti.lab2.task15;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.hub.CourseHubFacade;
import ru.sibguti.lab2.hub.EnrollmentBook;
import ru.sibguti.lab2.hub.HubResult;
import ru.sibguti.lab2.hub.InMemoryCourseRepository;
import ru.sibguti.lab2.hub.RepositoryUnavailableException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 15. Полная граница ошибок CourseHub (тесты зачисления, часть 2).
 * Класс Task15EnrollmentBoundaryTest проверяет use case зачисления: успешный
 * путь и все ожидаемые отказы (неверный код, курс не найден, набор не открыт,
 * повторная заявка, нет мест, не закрыт prerequisite) плюс инфраструктурный
 * отказ хранилища. Для каждого отказа проверяется ещё и неизменность состояния:
 * неудачная заявка не должна ни занимать место, ни попадать в журнал.
 * Зависимости: использует публичный слой CourseHubFacade, хранилище
 * InMemoryCourseRepository и доменные Course, CourseStatus, ErrorCode.
 */
class Task15EnrollmentBoundaryTest {

    private static final String STUDENT = "S-01";

    private InMemoryCourseRepository repository;
    private EnrollmentBook book;
    private CourseHubFacade hub;

    /*
     * Метод createFixture создаёт хранилище, журнал и публичный слой перед каждым
     * тестом и импортирует в них эталонный набор курсов из classpath-ресурса.
     * Благодаря @BeforeEach каждый тест начинается с одинакового состояния,
     * поэтому тесты не зависят ни от порядка запуска, ни от текущей директории.
     */
    @BeforeEach
    void createFixture() {
        repository = new InMemoryCourseRepository();
        book = new EnrollmentBook();
        hub = new CourseHubFacade(repository, book);
        HubResult importResult = hub.importCourses(openResource("/courses-ok.csv"));
        if (importResult.isSuccess() == false) {
            throw new IllegalStateException("подготовка теста: импорт не удался - " + importResult.getMessage());
        }
    }

    /*
     * Тест успешного зачисления: студент зачисляется, место занимается,
     * заявка попадает в журнал, наружу возвращается успех без кода ошибки.
     */
    @Test
    @DisplayName("успешное зачисление занимает место и пишет заявку в журнал")
    void successfulEnrollmentOccupiesSeatAndWritesJournal() {
        findCourse("CS101").setStatus(CourseStatus.OPEN);

        HubResult result = hub.enroll(STUDENT, "CS101");

        assertAll(
                () -> assertTrue(result.isSuccess(), "зачисление в открытый курс должно быть успешным"),
                () -> assertNull(result.getErrorCode(), "успех не содержит кода ошибки"),
                () -> assertEquals(1, findCourse("CS101").getEnrolledCount(), "место занято"),
                () -> assertTrue(book.hasApplication(STUDENT, "CS101"), "заявка записана в журнал")
        );
    }

    /*
     * Тест доменной валидации: неверный код курса - это ошибка данных,
     * поэтому наружу возвращается стабильный код E101, а исходная
     * IllegalArgumentException сохраняется в cause.
     */
    @Test
    @DisplayName("неверный код курса даёт INVALID_COURSE_CODE с сохранённой причиной")
    void invalidCourseCodeReturnsStableCode() {
        HubResult result = hub.enroll(STUDENT, "101");

        assertAll(
                () -> assertEquals(ErrorCode.INVALID_COURSE_CODE, result.getErrorCode(), "код ошибки данных"),
                () -> assertEquals(IllegalArgumentException.class, result.getCause().getClass(),
                        "исходная ошибка разбора кода сохранена в cause"),
                () -> assertFalse(book.hasApplication(STUDENT, "101"), "заявка не должна попасть в журнал")
        );
    }

    /*
     * Тест ожидаемого отказа: неизвестный код курса даёт COURSE_NOT_FOUND.
     */
    @Test
    @DisplayName("неизвестный курс даёт COURSE_NOT_FOUND")
    void unknownCourseReturnsCourseNotFound() {
        HubResult result = hub.enroll(STUDENT, "ZZ999");

        assertAll(
                () -> assertEquals(ErrorCode.COURSE_NOT_FOUND, result.getErrorCode(), "курс не найден"),
                () -> assertNull(result.getCause(), "это ожидаемый отказ, у него нет низкоуровневой причины")
        );
    }

    /*
     * Тест отказа по статусу: импортированные курсы имеют статус NEW,
     * поэтому зачисление в них отклоняется с кодом COURSE_NOT_OPEN.
     */
    @Test
    @DisplayName("курс в статусе NEW не принимает заявки")
    void newCourseIsNotOpenForEnrollment() {
        HubResult result = hub.enroll(STUDENT, "CS101");

        assertAll(
                () -> assertEquals(ErrorCode.COURSE_NOT_OPEN, result.getErrorCode(), "набор не открыт"),
                () -> assertEquals(0, findCourse("CS101").getEnrolledCount(), "место не должно заниматься")
        );
    }

    /*
     * Тест отказа из-за повторной заявки: сначала успешное зачисление,
     * затем тот же студент подаёт заявку ещё раз и получает ALREADY_ENROLLED.
     */
    @Test
    @DisplayName("повторная заявка даёт ALREADY_ENROLLED и не занимает второе место")
    void repeatedApplicationReturnsAlreadyEnrolled() {
        findCourse("CS101").setStatus(CourseStatus.OPEN);
        hub.enroll(STUDENT, "CS101");

        HubResult result = hub.enroll(STUDENT, "CS101");

        assertAll(
                () -> assertEquals(ErrorCode.ALREADY_ENROLLED, result.getErrorCode(), "повторная заявка"),
                () -> assertEquals(1, findCourse("CS101").getEnrolledCount(),
                        "второе место не должно заниматься")
        );
    }

    /*
     * Тест отказа из-за отсутствия мест: у курса CS103 вместимость 20,
     * места занимаются до конца, после чего заявка отклоняется.
     */
    @Test
    @DisplayName("отсутствие мест даёт NO_FREE_SEATS")
    void noFreeSeatsReturnsNoFreeSeats() {
        Course course = findCourse("CS103");
        course.setStatus(CourseStatus.OPEN);
        fillCourse(course);

        HubResult result = hub.enroll(STUDENT, "CS103");

        assertAll(
                () -> assertEquals(ErrorCode.NO_FREE_SEATS, result.getErrorCode(), "мест нет"),
                () -> assertEquals(course.getCapacity(), course.getEnrolledCount(),
                        "наполненность не должна увеличиться")
        );
    }

    /*
     * Тест отказа по prerequisite: курс CS102 требует закрытый CS101.
     * Без закрытого CS101 заявка отклоняется с кодом PREREQUISITE_MISSING.
     */
    @Test
    @DisplayName("незакрытый prerequisite даёт PREREQUISITE_MISSING")
    void missingPrerequisiteReturnsPrerequisiteMissing() {
        findCourse("CS102").setStatus(CourseStatus.OPEN);

        HubResult result = hub.enroll(STUDENT, "CS102");

        assertAll(
                () -> assertEquals(ErrorCode.PREREQUISITE_MISSING, result.getErrorCode(),
                        "предварительная дисциплина не закрыта"),
                () -> assertFalse(book.hasApplication(STUDENT, "CS102"), "заявка не записана")
        );
    }

    /*
     * Тест успешного пути с prerequisite: закрываем CS101 в журнале,
     * после чего заявка на CS102 проходит.
     */
    @Test
    @DisplayName("закрытый prerequisite разрешает зачисление")
    void completedPrerequisiteAllowsEnrollment() {
        book.markCompleted(STUDENT, "CS101");
        findCourse("CS102").setStatus(CourseStatus.OPEN);

        HubResult result = hub.enroll(STUDENT, "CS102");

        assertAll(
                () -> assertTrue(result.isSuccess(), "с закрытым prerequisite зачисление успешно"),
                () -> assertEquals(1, findCourse("CS102").getEnrolledCount(), "место занято")
        );
    }

    /*
     * Тест инфраструктурного отказа: хранилище недоступно, наружу возвращается
     * SERVICE_UNAVAILABLE с сохранённой причиной, а состояние не меняется.
     * Ссылку на курс берём ДО отключения хранилища: после отказа поиск в каталоге
     * сам бросает RepositoryUnavailableException, и проверять состояние пришлось бы
     * по сохранённой ссылке, а не по повторному запросу в недоступный сервис.
     */
    @Test
    @DisplayName("недоступный каталог даёт SERVICE_UNAVAILABLE и не меняет состояние")
    void unavailableRepositoryReturnsServiceUnavailable() {
        Course course = findCourse("CS101");
        course.setStatus(CourseStatus.OPEN);
        repository.setUnavailable(true);

        HubResult result = hub.enroll(STUDENT, "CS101");

        assertAll(
                () -> assertEquals(ErrorCode.SERVICE_UNAVAILABLE, result.getErrorCode(),
                        "инфраструктурный код отказа"),
                () -> assertEquals(RepositoryUnavailableException.class, result.getCause().getClass(),
                        "исходная инфраструктурная ошибка сохранена"),
                () -> assertEquals(0, course.getEnrolledCount(), "место не занято"),
                () -> assertFalse(book.hasApplication(STUDENT, "CS101"), "заявка не записана")
        );
    }

    /*
     * Негативный параметризованный тест: ожидаемые отказы зачисления
     * всегда возвращаются стабильным кодом, а не исключением.
     * Колонки: код курса, статус, свободны ли места, закрыт ли prerequisite,
     * ожидаемый код отказа.
     */
    @ParameterizedTest(name = "{0}: статус {1}, места {2}, prerequisite {3} -> {4}")
    @CsvSource({
            "CS101, NEW, true, true, E103",
            "CS101, CLOSED, true, true, E103",
            "CS101, CANCELLED, true, true, E103",
            "CS102, OPEN, true, false, E105",
            "CS103, OPEN, false, true, E104"
    })
    @DisplayName("ожидаемые отказы зачисления возвращаются кодами, а не исключениями")
    void expectedRejectionsReturnStableCodes(String courseCode, String statusName, boolean freeSeats,
                                             boolean prerequisiteClosed, String expectedCode) {
        Course course = findCourse(courseCode);
        course.setStatus(CourseStatus.valueOf(statusName));
        if (freeSeats == false) {
            fillCourse(course);
        }
        if (prerequisiteClosed && course.getPrerequisiteCode() != null) {
            book.markCompleted(STUDENT, course.getPrerequisiteCode());
        }

        HubResult result = hub.enroll(STUDENT, courseCode);
        final int occupiedBefore = course.getEnrolledCount();

        assertAll(
                () -> assertFalse(result.isSuccess(), "ожидался отказ, а не успех"),
                () -> assertEquals(expectedCode, result.getErrorCode().code(), "стабильный код отказа"),
                () -> assertFalse(book.hasApplication(STUDENT, courseCode),
                        "неудачная заявка не должна попасть в журнал"),
                () -> assertEquals(occupiedBefore, course.getEnrolledCount(),
                        "неудачная заявка не должна занимать место")
        );
    }

    /*
     * Тест полного сценария "импорт, потом зачисление": данные уже импортированы
     * в createFixture, курс открывается, студент зачисляется. Код приходит
     * в неопрятном виде " cs-101 " и нормализуется на границе.
     */
    @Test
    @DisplayName("полный сценарий: импорт файла и зачисление студента")
    void importThenEnrollmentScenario() {
        findCourse("CS101").setStatus(CourseStatus.OPEN);

        HubResult result = hub.enroll(STUDENT, " cs-101 ");

        assertAll(
                () -> assertTrue(result.isSuccess(), "код с пробелами и дефисом нормализуется"),
                () -> assertEquals(1, findCourse("CS101").getEnrolledCount(), "место занято")
        );
    }

    /*
     * Вспомогательный метод findCourse достаёт курс из публичного хранилища,
     * чтобы тесты проверяли состояние курса после зачисления или отказа.
     * Его вызывают почти все тесты этого класса. Ловится только конкретный
     * инфраструктурный отказ, а не Exception: тест сам должен быть точным.
     */
    private Course findCourse(String code) {
        try {
            return repository.findByCode(code).orElseThrow(
                    () -> new IllegalStateException("курс не найден в тестовых данных: " + code));
        } catch (RepositoryUnavailableException exception) {
            throw new IllegalStateException("каталог недоступен в тесте", exception);
        }
    }

    /*
     * Вспомогательный метод fillCourse занимает все места в курсе, чтобы
     * получить состояние "мест нет". Его вызывает тест отказа по местам
     * и параметризованный тест.
     */
    private void fillCourse(Course course) {
        while (course.hasFreeSeats()) {
            course.enrollOne();
        }
    }

    /*
     * Метод openResource открывает файл из classpath, чтобы тест не зависел
     * от текущей директории. Его вызывает createFixture.
     */
    private Reader openResource(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("ресурс не найден: " + path);
        }
        return new InputStreamReader(stream, StandardCharsets.UTF_8);
    }
}

package ru.sibguti.lab2.hub;

import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.CourseCode;
import ru.sibguti.lab2.domain.EnrollmentDecision;
import ru.sibguti.lab2.domain.EnrollmentService;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.task14.CourseImporter;
import ru.sibguti.lab2.task14.ImportFailedException;
import ru.sibguti.lab2.task14.ImportReport;

import java.io.Reader;
import java.util.Optional;

/*
 * Задание 15. Полная граница ошибок CourseHub.
 * Этот класс - публичный слой: только он переводит внутренние ошибки
 * в стабильные коды ErrorCode, которые видит клиент (UI, скрипт, другой сервис).
 *
 * КАКИЕ ИСКЛЮЧЕНИЯ ЛОВЯТСЯ НА КАЖДОМ УРОВНЕ И ПОЧЕМУ:
 * 1) Уровень разбора записи (CourseImporter, задание 14) ловит только
 *    NumberFormatException из разбора чисел и IllegalArgumentException из
 *    конструкторов домена: это ошибки данных, и им нужен номер записи.
 *    Он также ловит IOException источника и RepositoryUnavailableException
 *    хранилища. Ловить Exception здесь нельзя.
 * 2) Уровень хранилища (CourseRepository) объявляет checked-исключение
 *    RepositoryUnavailableException: отказ внешний, его можно повторить.
 * 3) Уровень бизнес-правил (EnrollmentService, задание 13) исключений не бросает
 *    вообще: он возвращает решение с кодом отказа, потому что отказ зачисления -
 *    это ожидаемый исход, а не поломка.
 * 4) Уровень домена (Course, CourseCode) бросает IllegalArgumentException
 *    (нарушение контракта) и IllegalStateException (нарушение состояния).
 * 5) Публичный слой (этот класс) ловит РОВНО перечисленные типы и превращает их
 *    в коды ошибок. Наружу из него не выходит ни одно исключение, кроме
 *    программных ошибок.
 *
 * ПОЧЕМУ ЗАПРЕЩЁН ГЛОБАЛЬНЫЙ catch (Exception):
 *  - он скрывает и настоящие дефекты кода (NullPointerException, опечатки в полях);
 *  - он превращает разные отказы (нет файла, нет места, сломанный формат)
 *    в один и тот же код, и клиент перестаёт различать ситуации;
 *  - после него невозможно понять, что чинить: правило, конфигурацию или код;
 *  - он всё равно не ловит Error (OutOfMemoryError, StackOverflowError),
 *    то есть не выполняет своей задачи.
 * Программные ошибки (IllegalStateException, NullPointerException, Error)
 * намеренно НЕ ловятся: они должны падать сразу и loudly, потому что
 * это дефект программы, а не отказ бизнес-логики.
 *
 * Зависимости: использует доменные Course, CourseCode, EnrollmentService и
 * ErrorCode, хранилище CourseRepository, журнал EnrollmentBook и импортёр
 * CourseImporter из задания 14. Тесты задания 15 вызывают его два публичных метода.
 */
public class CourseHubFacade {

    private final CourseRepository repository;
    private final EnrollmentBook book;
    private final EnrollmentService rules;
    private final CourseImporter importer;

    /*
     * Конструктор создаёт публичный слой поверх переданных хранилища и журнала,
     * а импортёр собирает из них. Его вызывают сценарии и каждый тест задания 15,
     * чтобы получить полностью изолированный набор данных для одного теста.
     */
    public CourseHubFacade(CourseRepository repository, EnrollmentBook book) {
        this.repository = repository;
        this.book = book;
        this.rules = new EnrollmentService();
        this.importer = new CourseImporter(repository);
    }

    public EnrollmentBook getBook() {
        return book;
    }

    public CourseRepository getRepository() {
        return repository;
    }

    /*
     * Метод importCourses импортирует курсы из Reader и никогда не бросает
     * наружу доменные и инфраструктурные исключения: вместо них возвращается
     * HubResult со стабильным кодом. Все записи публикуются только целиком,
     * поэтому частичного результата не бывает, а исходная причина отказа
     * сохраняется в cause для журнала. Его вызывают тесты задания 15.
     */
    public HubResult importCourses(Reader reader) {
        ImportReport report;
        try {
            report = importer.importFrom(reader);
        } catch (ImportFailedException exception) {
            return HubResult.error(exception.getErrorCode(),
                    "импорт не завершён: " + exception.getMessage(), exception);
        }
        return HubResult.ok("импортировано записей: " + report.getRecordsImported());
    }

    /*
     * Метод enroll зачисляет студента и возвращает HubResult. Порядок проверок
     * соответствует уровням: сначала нормализация кода (домен), потом поиск курса
     * (хранилище), потом бизнес-правила (EnrollmentService), и только потом
     * изменение состояния. Ошибки данных и хранилища превращаются в коды,
     * а IllegalStateException из состояния курса намеренно пробрасывается:
     * это программная ошибка, которую должен увидеть разработчик.
     * Его вызывают тесты задания 15 и сценарии use case зачисления.
     */
    public HubResult enroll(String studentId, String rawCourseCode) {
        CourseCode code;
        try {
            code = CourseCode.of(rawCourseCode);
        } catch (IllegalArgumentException exception) {
            return HubResult.error(ErrorCode.INVALID_COURSE_CODE,
                    "код курса не соответствует формату", exception);
        }

        Course course;
        try {
            Optional<Course> found = repository.findByCode(code.value());
            course = found.orElse(null);
        } catch (RepositoryUnavailableException exception) {
            return HubResult.error(ErrorCode.SERVICE_UNAVAILABLE,
                    "каталог курсов временно недоступен", exception);
        }

        if (course == null) {
            return HubResult.error(ErrorCode.COURSE_NOT_FOUND,
                    "курс " + code.value() + " не найден");
        }

        boolean alreadyApplied = book.hasApplication(studentId, code.value());
        boolean prerequisiteMet = book.hasCompletedPrerequisite(studentId, course.getPrerequisiteCode());

        EnrollmentDecision decision = rules.decide(course.getStatus(), course.hasFreeSeats(),
                prerequisiteMet, alreadyApplied);
        if (!decision.isAccepted()) {
            return HubResult.error(decision.getError(), decision.getComment());
        }

        course.enrollOne();
        book.addApplication(studentId, code.value());
        return HubResult.ok("студент " + studentId + " зачислен в " + code.value());
    }
}

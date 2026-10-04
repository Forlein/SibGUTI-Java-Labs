package ru.sibguti.lab2.task07;

import ru.sibguti.lab2.Console;
import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.CourseCode;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.EnrollmentDecision;
import ru.sibguti.lab2.domain.EnrollmentService;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.task03.EnrollmentRejectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Задание 7. Ошибка как значение или исключение.
 * Класс Task7CourseService содержит четыре операции, и для каждой выбран
 * свой способ сообщать об отказе:
 *   findCourse     -> Optional (курс не найден - ожидаемый результат);
 *   parseCourse    -> типизированный result (нужен отчёт по всем записям);
 *   enroll         -> checked exception (бизнес-отказ, вызывающий обязан решить);
 *   calculatePrice -> unchecked exception (нарушение контракта, ошибка программиста).
 * Ни одна из операций не бросает исключение там, где отказ заранее ожидаем,
 * и ни одна не прячет программную ошибку в "пустой" результат.
 * Зависимости: использует доменные Course, CourseCode, EnrollmentService и
 * ErrorCode, а также собственное исключение EnrollmentRejectedException
 * из задания 3 - доменный отказ зачисления у всех операций должен быть один.
 */
public class Task7CourseService {

    private static final String SEPARATOR = ";";
    private static final int MIN_HOURS = Course.MIN_DURATION_HOURS;
    private static final int MAX_HOURS = Course.MAX_DURATION_HOURS;

    private final List<Course> catalog = new ArrayList<Course>();
    private final EnrollmentService rules = new EnrollmentService();

    /*
     * Метод findCourse ищет курс по уже нормализованному коду и возвращает
     * Optional<Course>: "курса нет" - это ожидаемый исход поиска, а не авария,
     * поэтому исключение здесь было бы лишним. Пустой Optional вызывающий
     * обязан обработать, но компилятор напоминать об этом не будет.
     * Его вызывают enroll, главный метод и тесты задания 7.
     */
    public Optional<Course> findCourse(CourseCode code) {
        for (int i = 0; i < catalog.size(); i++) {
            if (catalog.get(i).getCode().equals(code)) {
                return Optional.of(catalog.get(i));
            }
        }
        return Optional.empty();
    }

    /*
     * Метод parseCourse разбирает строку вида "CS101;Основы Java;72" и всегда
     * возвращает типизированный результат: либо готовый Course, либо код ошибки
     * с номером записи. Исключения не бросаются, потому что импорт файла должен
     * показать все плохие записи сразу, а не падать на первой.
     * Его вызывает главный метод задания.
     */
    public Task7CourseParseResult parseCourse(String rawLine, int recordNumber) {
        String[] fields = rawLine.split(SEPARATOR);
        if (fields.length != 3) {
            return Task7CourseParseResult.fail(recordNumber, ErrorCode.MALFORMED_RECORD,
                    "ожидалось 3 поля, получено " + fields.length);
        }

        CourseCode code;
        try {
            code = CourseCode.of(fields[0]);
        } catch (IllegalArgumentException exception) {
            return Task7CourseParseResult.fail(recordNumber, ErrorCode.INVALID_COURSE_CODE,
                    exception.getMessage());
        }

        int hours;
        try {
            hours = Integer.parseInt(fields[2].trim());
        } catch (NumberFormatException exception) {
            return Task7CourseParseResult.fail(recordNumber, ErrorCode.MALFORMED_RECORD,
                    "поле hours не является числом");
        }

        if (hours < MIN_HOURS || hours > MAX_HOURS) {
            return Task7CourseParseResult.fail(recordNumber, ErrorCode.HOURS_OUT_OF_RANGE,
                    "ожидалось " + MIN_HOURS + ".." + MAX_HOURS + " часов, получено " + hours);
        }

        return Task7CourseParseResult.ok(new Course(code.value(), fields[1], hours, 30), recordNumber);
    }

    /*
     * Метод enroll зачисляет студента и объявляет checked-исключение
     * EnrollmentRejectedException: это бизнес-отказ с известным кодом причины,
     * и вызывающий обязан его обработать (например, вернуть код ошибки клиенту).
     * Программные ошибки (например, испорченный код) сюда не превращаются -
     * они остаются unchecked и летят дальше.
     * Его вызывает главный метод задания и обработчик границы в нём же.
     */
    public void enroll(String studentId, CourseCode code, boolean alreadyApplied, boolean prerequisiteMet)
            throws EnrollmentRejectedException {
        Course course = findCourse(code).orElse(null);
        if (course == null) {
            throw new EnrollmentRejectedException(ErrorCode.COURSE_NOT_FOUND, studentId, code.value());
        }

        EnrollmentDecision decision = rules.decide(course.getStatus(), course.hasFreeSeats(),
                prerequisiteMet, alreadyApplied);
        if (!decision.isAccepted()) {
            throw new EnrollmentRejectedException(decision.getError(), studentId, code.value());
        }
        course.enrollOne();
    }

    /*
     * Метод calculatePrice считает цену курса и бросает unchecked-исключения
     * при нарушении контракта: скидка вне 0..100 или отрицательная цена.
     * Это ошибки программиста (константы пришли не из пользовательского ввода),
     * поэтому превращать их в пустой результат нельзя - это замаскирует баг.
     * Его вызывает главный метод задания в контрастном случае программной ошибки.
     */
    public double calculatePrice(double basePrice, int discountPercent) {
        if (basePrice < 0) {
            throw new IllegalArgumentException("calculatePrice: базовая цена не может быть отрицательной");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("calculatePrice: скидка должна быть в диапазоне 0..100");
        }
        return basePrice - basePrice * discountPercent / 100.0;
    }

    /*
     * Главный метод печатает таблицу выбранных стратегий, а затем два
     * контрастных случая: ожидаемый отказ (превращается в значение) и
     * программная ошибка (остаётся исключением). Его вызывает среда разработки.
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 7. ОШИБКА КАК ЗНАЧЕНИЕ ИЛИ ИСКЛЮЧЕНИЕ");
        System.out.println();

        printStrategyTable();

        Task7CourseService service = new Task7CourseService();
        Task7CourseParseResult good = service.parseCourse("CS101;Основы Java;72", 1);
        Task7CourseParseResult bad = service.parseCourse("CS-102;Базы данных;0", 2);
        Task7CourseParseResult ugly = service.parseCourse("не запись", 3);
        service.getCatalogForDemo().add(good.getCourse());
        good.getCourse().setStatus(CourseStatus.OPEN);

        System.out.println("--- Контрастный случай 1: ОЖИДАЕМЫЙ отказ становится значением ---");
        System.out.println("parseCourse (плохая длительность): " + bad);
        System.out.println("parseCourse (мусор в строке)       : " + ugly);
        Optional<Course> notFound = service.findCourse(CourseCode.of("ZZ999"));
        System.out.println("findCourse (нет такого курса)    : " + (notFound.isPresent() ? "найден" : "Optional.empty"));
        System.out.println("исключений не было, вызывающий получил данные и разбирается сам");
        System.out.println();

        System.out.println("--- Контрастный случай 1 (продолжение): бизнес-отказ зачисления ---");
        try {
            service.enroll("S-01", CourseCode.of("CS101"), false, false);
        } catch (EnrollmentRejectedException exception) {
            System.out.println("поймано checked-исключение: " + exception.getMessage());
            System.out.println("на границе это превратится в код " + exception.getReasonCode().code());
        }
        System.out.println();

        System.out.println("--- Контрастный случай 2: ПРОГРАММНАЯ ошибка остаётся исключением ---");
        System.out.println("цена без скидки: " + service.calculatePrice(10000.0, 0));
        try {
            service.calculatePrice(10000.0, 150);
        } catch (IllegalArgumentException exception) {
            System.out.println("поймано unchecked-исключение: " + exception.getMessage());
            System.out.println("превращать это в пустой результат нельзя: баг должен быть виден сразу");
        }
    }

    /*
     * Метод printStrategyTable печатает четыре выбранных способа сообщать
     * об отказе и короткое объяснение выбора. Его вызывает главный метод.
     */
    private static void printStrategyTable() {
        System.out.println("Операция        | Выбранный способ            | Почему");
        System.out.println("findCourse      | Optional                    | отсутствие курса - ожидаемый исход поиска");
        System.out.println("parseCourse     | типизированный result       | нужен отчёт по всем записям файла сразу");
        System.out.println("enroll          | checked exception           | бизнес-отказ, вызывающий обязан обработать");
        System.out.println("calculatePrice  | unchecked exception         | нарушение контракта, ошибка программиста");
        System.out.println();
    }

    /*
     * Метод getCatalogForDemo отдаёт список курсов сервиса, чтобы главный
     * метод задания мог положить в него разобранный курс. В рабочем коде
     * каталог пополняет импорт, здесь это делает демонстрация.
     */
    public List<Course> getCatalogForDemo() {
        return catalog;
    }
}

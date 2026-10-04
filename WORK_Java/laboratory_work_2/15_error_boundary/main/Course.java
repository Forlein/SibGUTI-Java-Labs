package ru.sibguti.lab2.domain;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 7, перенесён в задание 15 без изменений.
 * Зачем он нужен в этом задании: описание курса каталога: длительность, вместимость, статус, закрытые часы.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс Course описывает курс каталога: код, название, длительность в часах,
 * вместимость, статус, число зачисленных студентов и уже закрытые часы.
 * Длительность проверяется в конструкторе, потому что это данные из файла
 * или из формы: значения вне диапазона 16..240 часов - это ошибка данных,
 * а не ошибка программиста, и на границе импорта её превращают в код ошибки.
 * Зависимости: его создаёт импортёр (задания 6, 14), его часы изменяет
 * completeHours (тесты заданий 8, 9, 11), а EnrollmentService (задание 13)
 * и CourseHubFacade (задание 15) читают статус, наполненность, остаток часов
 * и код предварительной дисциплины.
 */
public class Course {

    public static final int MIN_DURATION_HOURS = 16;
    public static final int MAX_DURATION_HOURS = 240;

    private final CourseCode code;
    private final String title;
    private final int durationHours;
    private final int capacity;
    private final String prerequisiteCode;
    private CourseStatus status;
    private int completedHours;
    private int enrolledCount;

    /*
     * Конструктор проверяет длительность и вместимость и нормализует код курса.
     * Нарушения контракта он сообщает как IllegalArgumentException (unchecked):
     * если граница не перевела эти данные раньше, то это ошибка самого вызова.
     * Его вызывают импортёр (задания 6 и 14) и тесты заданий 8, 9, 10, 11.
     */
    public Course(String rawCode, String title, int durationHours, int capacity) {
        this(rawCode, title, durationHours, capacity, null);
    }

    /*
     * Конструктор с дополнительным кодом предварительной дисциплины нужен
     * таблице решений задания 13 и зачислению в задании 15: без закрытой
     * предварительной дисциплины заявка отклоняется. Если код не задан,
     * prerequisite считается выполненным. Его вызывает импортёр и тесты задания 15.
     */
    public Course(String rawCode, String title, int durationHours, int capacity, String prerequisiteRawCode) {
        if (durationHours < MIN_DURATION_HOURS || durationHours > MAX_DURATION_HOURS) {
            throw new IllegalArgumentException(
                    "Course: длительность курса вне диапазона " + MIN_DURATION_HOURS + ".." + MAX_DURATION_HOURS + " часов");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Course: вместимость должна быть положительным числом");
        }
        this.code = CourseCode.of(rawCode);
        this.title = title;
        this.durationHours = durationHours;
        this.capacity = capacity;
        if (prerequisiteRawCode == null) {
            this.prerequisiteCode = null;
        } else {
            this.prerequisiteCode = CourseCode.of(prerequisiteRawCode).value();
        }
        this.status = CourseStatus.NEW;
        this.completedHours = 0;
        this.enrolledCount = 0;
    }

    /*
     * Метод getPrerequisiteCode возвращает код предварительной дисциплины
     * или null, если её нет. Его читает EnrollmentBook (задание 15), когда
     * проверяет, выполнено ли условие зачисления.
     */
    public String getPrerequisiteCode() {
        return prerequisiteCode;
    }

    /*
     * Метод completeHours закрывает указанное число часов курса и возвращает
     * ничего: состояние меняется в самом объекте. Ноль или отрицательное
     * число часов - ошибка программиста (IllegalArgumentException), а попытка
     * закрыть больше, чем осталось, - нарушение состояния (IllegalStateException).
     * Исключения бросаются до изменения полей, поэтому объект остаётся неизменным.
     * Его вызывают тесты заданий 8, 9 и 11, а также поток закрытия курса в задании 15.
     */
    public void completeHours(int hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Course: количество часов должно быть положительным");
        }
        if (completedHours + hours > durationHours) {
            throw new IllegalStateException(
                    "Course: нельзя закрыть " + hours + " ч., доступно только " + remainingHours() + " ч.");
        }
        completedHours = completedHours + hours;
    }

    /*
     * Метод hasFreeSeats сообщает, остались ли свободные места.
     * Его читает EnrollmentService при проверке таблицы решений (задание 13)
     * и CourseHubFacade перед зачислением студента (задание 15).
     */
    public boolean hasFreeSeats() {
        return enrolledCount < capacity;
    }

    /*
     * Метод getFreeSeatsCount возвращает число свободных мест и нужен
     * для сообщения об отказе "мест нет" и для проверок в тестах задания 15.
     */
    public int getFreeSeatsCount() {
        return capacity - enrolledCount;
    }

    /*
     * Метод enrollOne занимает одно место в курсе. Если мест нет, он бросает
     * IllegalStateException, потому что это нарушение состояния объекта.
     * Проверка и изменение идут подряд, поэтому состояние не ломается.
     * Его вызывает CourseHubFacade после успешного решения таблицы правил.
     */
    public void enrollOne() {
        if (!hasFreeSeats()) {
            throw new IllegalStateException("Course: свободных мест нет");
        }
        enrolledCount = enrolledCount + 1;
    }

    /*
     * Метод getEnrolledCount возвращает число уже занятых мест.
     * Его используют EnrollmentBook (задание 15) и тесты, проверяющие,
     * что неудачная заявка не изменила состояние курса (задания 11, 15).
     */
    public int getEnrolledCount() {
        return enrolledCount;
    }

    /*
     * Метод isCompleted сообщает, закрыты ли все часы курса.
     * Его читают отчёты и тесты задания 11, чтобы убедиться, что объект
     * достиг ожидаемого состояния ровно один раз.
     */
    public boolean isCompleted() {
        return completedHours == durationHours;
    }

    /*
     * Метод remainingHours возвращает, сколько часов ещё осталось закрыть.
     * Его использует completeHours для текста исключения и тесты границ (задание 9).
     */
    public int remainingHours() {
        return durationHours - completedHours;
    }

    public CourseCode getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public int getCapacity() {
        return capacity;
    }

    public CourseStatus getStatus() {
        return status;
    }

    /*
     * Метод setStatus меняет состояние курса (например, открывает набор).
     * Его вызывают сценарии и тесты задания 13, чтобы подготовить курс
     * с нужным статусом до проверки таблицы решений.
     */
    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public int getCompletedHours() {
        return completedHours;
    }

    public String toString() {
        return code.value() + " " + title + " (" + completedHours + "/" + durationHours + " ч.)";
    }
}

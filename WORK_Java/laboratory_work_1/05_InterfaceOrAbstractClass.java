import java.time.Instant;

/**
 * Задание 5. «Интерфейс или абстрактный класс».
 *
 * Для пяти понятий (Identifiable, BaseEntity, CoursePolicy, Formatter<T>,
 * ClockProvider) выбрана форма контракта и обоснована цена ошибки:
 * чистое поведение без состояния и открытая иерархия — интерфейс;
 * общие данные плюс общий код equals/hashCode/toString — абстрактный класс.
 * Итоговые решения печатаются таблицей в конце.
 *
 * Запуск: javac 05_InterfaceOrAbstractClass.java
 *         java  -cp . InterfaceOrAbstractClass
 */
class InterfaceOrAbstractClass {

    /**
     * Минимальный контракт «я идентифицируем». Выбран интерфейс: носителей
     * много, они не связаны наследованием, состояние и общее поведение
     * не нужны. Используется как тип элементов реестра Map<String, Identifiable>.
     */
    interface Identifiable {

        /**
         * Возвращает уникальный в пределах системы идентификатор.
         * Именно он используется как ключ в реестре main, как часть
         * equals/hashCode в BaseEntity и в правилах AgePolicy
         * и CourseLevelPolicy.
         */
        String id();
    }

    /**
     * Абстрактный класс — общий предок всех сущностей. Выбран класс,
     * потому что нужны и состояние (id, createdAt, updatedAt), и общий
     * код equals/hashCode, который не должен «приклеиваться» ко всем
     * реализациям Identifiable. Наследуется ровно одним родителем —
     * это осознанная цена решения.
     */
    abstract static class BaseEntity implements Identifiable {

        private final String id;
        private final Instant createdAt;
        private String updatedAt;

        /**
         * Конструктор общей части сущности: принимает id и время создания,
         * а если id не задан — генерирует его через System.identityHashCode(this)
         * (статический метод, безопасный в конструкторе). Вызывается
         * из конструкторов Student и Course для передачи общих данных наверх.
         */
        protected BaseEntity(String id, Instant createdAt) {
            if (id == null || id.trim().isEmpty()) {
                this.id = "GEN-" + Integer.toHexString(System.identityHashCode(this));
            } else {
                this.id = id;
            }
            this.createdAt = createdAt;
            this.updatedAt = String.valueOf(createdAt);
        }

        /**
         * Реализация контракта Identifiable. Возвращает идентификатор,
         * заданный конструктором (или сгенерированный). Вызывается из main
         * при печати авто-id, из equals/hashCode этого же класса,
         * а также правилами AgePolicy и CourseLevelPolicy.
         */
        @Override
        public final String id() {
            return id;
        }

        /**
         * Возвращает момент создания сущности. Значение неизменно,
         * поэтому метод final. Используется в toString() и доступно
         * клиенту main при выводе состояния.
         */
        public final Instant getCreatedAt() {
            return createdAt;
        }

        /**
         * Возвращает строковое представление времени последнего изменения.
         * Используется в toString() и обновляется только через touch(...),
         * чтобы все изменения шли через одну дверь.
         */
        public final String getUpdatedAt() {
            return updatedAt;
        }

        /**
         * Единственный разрешённый способ изменить updatedAt: принимает
         * момент времени и записывает его. Вызывается клиентом, которому
         * нужно отметить изменение сущности; прямой доступ к полю закрыт.
         */
        public final void touch(Instant when) {
            this.updatedAt = String.valueOf(when);
        }

        /**
         * Общее правило равенства: объекты равны, если у них одинаковый
         * класс И одинаковый id. Вызывается в main при сравнении двух
         * студентов с одним id и при сравнении студента с курсом;
         * строгое сравнение getClass() исключает сравнение «всё со всем».
         */
        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BaseEntity other = (BaseEntity) o;
            return id.equals(other.id);
        }

        /**
         * Общий хэш по идентификатору, согласованный с equals:
         * равные объекты обязаны вернуть одинаковый хэш, иначе они
         * «разъедутся» по корзинам HashSet/HashMap. Используется
         * платформой коллекций при работе с сущностями.
         */
        @Override
        public final int hashCode() {
            return id.hashCode();
        }

        /**
         * Общее текстовое представление сущности: имя класса, id и время
         * создания. Имя берётся через getClass(), поэтому подклассы
         * печатаются корректно. Вызывается из main при выводе Student и Course.
         */
        @Override
        public String toString() {
            return getClass().getSimpleName() + "{id=" + id
                    + ", createdAt=" + createdAt + "}";
        }
    }

    /**
     * Конкретная сущность «студент»: наследует всю общую логику
     * BaseEntity и добавляет только ФИО.
     */
    static class Student extends BaseEntity {

        private final String fullName;

        /**
         * Конструктор студента: передаёт id и createdAt в super(...),
         * затем сохраняет ФИО. Вызывается из main при создании
         * кандидатов и участников эксперимента с equals.
         */
        Student(String id, Instant createdAt, String fullName) {
            super(id, createdAt);
            this.fullName = fullName;
        }

        /**
         * Возвращает ФИО студента. Используется StudentTextFormatter
         * при формировании строки и toString() сущности.
         */
        String getFullName() {
            return fullName;
        }
    }

    /**
     * Конкретная сущность «курс»: наследует общую логику BaseEntity
     * и добавляет только название.
     */
    static class Course extends BaseEntity {

        private final String title;

        /**
         * Конструктор курса: передаёт общие данные в super(...) и сохраняет
         * название. Вызывается из main при формировании каталога курсов.
         */
        Course(String id, Instant createdAt, String title) {
            super(id, createdAt);
            this.title = title;
        }

        /**
         * Возвращает название курса. Используется CourseTextFormatter
         * и renderAny(...) при печати типизированного значения.
         */
        String getTitle() {
            return title;
        }
    }

    /**
     * Контракт «правило допуска к курсу» — открытый интерфейс-стратегия.
     * Правила не имеют состояния, подставляются в клиент как стратегии,
     * а их набор постоянно расширяется внешними командами.
     */
    interface CoursePolicy {

        /**
         * Возвращает короткое имя правила для отчётов и логов.
         * Вызывается в main при печати результата проверки.
         */
        String name();

        /**
         * Проверяет кандидата на соответствие правилу.
         * Вызывается из main в двойном цикле по правилам и курсам;
         * конкретное тело выбирается по фактическому типу стратегии.
         */
        boolean isSatisfiedBy(Student student, Course course);
    }

    /**
     * Стратегия «есть 18 лет». Состояние стратегии — минимальный возраст,
     * то есть настройка, а не отдельный подкласс.
     */
    static class AgePolicy implements CoursePolicy {

        private final int minAge;

        /**
         * Задаёт минимальный возраст. Вызывается из main при сборке
         * массива правил для проверки одного кандидата.
         */
        AgePolicy(int minAge) {
            this.minAge = minAge;
        }

        /**
         * Возвращает имя правила вместе с настройкой. Вызывается в main
         * при печати результата проверки.
         */
        @Override
        public String name() {
            return "age>=" + minAge;
        }

        /**
         * Считает возраст кандидата (в демо — выводит из id) и сравнивает
         * с minAge. Вызывается полиморфно из main для каждой пары
         * «правило + курс».
         */
        @Override
        public boolean isSatisfiedBy(Student student, Course course) {
            int age = 18 + Math.abs(student.id().hashCode() % 5);
            return age >= minAge;
        }
    }

    /**
     * Стратегия «курс не раньше третьего курса». Тот же контракт,
     * никакого состояния — демонстрация минимальной стратегии.
     */
    static class CourseLevelPolicy implements CoursePolicy {

        /**
         * Возвращает имя правила "level>=3". Вызывается в main
         * при печати результата проверки.
         */
        @Override
        public String name() {
            return "level>=3";
        }

        /**
         * Проверяет, что id кандидата заканчивается на 3 или 4.
         * Вызывается полиморфно из main для каждой пары «правило + курс».
         */
        @Override
        public boolean isSatisfiedBy(Student student, Course course) {
            return student.id().endsWith("3") || student.id().endsWith("4");
        }
    }

    /**
     * Обобщённый контракт «превратить объект типа T в строку».
     * Чистая функция без состояния, поэтому интерфейс; generics нужны,
     * чтобы один контракт обслуживал и Student, и Course.
     */
    interface Formatter<T> {

        /**
         * Превращает значение в строку. Вызывается напрямую в main
         * у типизированных переменных sf и cf, а также внутри renderAny(...)
         * для произвольного типа T.
         */
        String format(T value);
    }

    /**
     * Форматтер студента: реализация Formatter<Student>.
     */
    static class StudentTextFormatter implements Formatter<Student> {

        /**
         * Возвращает «ФИО (id)». Вызывается в main через переменную
         * типа Formatter<Student>.
         */
        @Override
        public String format(Student value) {
            return value.getFullName() + " (" + value.id() + ")";
        }
    }

    /**
     * Форматтер курса: реализация Formatter<Course>.
     */
    static class CourseTextFormatter implements Formatter<Course> {

        /**
         * Возвращает «название [id]». Вызывается в main напрямую
         * и внутри renderAny(...) через Formatter<? super Course>.
         */
        @Override
        public String format(Course value) {
            return value.getTitle() + " [" + value.id() + "]";
        }
    }

    /**
     * Форматирует произвольное значение T форматтером, умеющим работать
     * с T или с чем-то старше T. Вызывается в main для Course с
     * CourseTextFormatter; тип T выводится компилятором из первого аргумента.
     */
    static <T> String renderAny(T value, Formatter<? super T> f) {
        return f.format(value);
    }

    /**
     * Контракт «источник текущего времени». Нужен, чтобы время можно было
     * подменить в тестах: System.currentTimeMillis() в коде делает тесты
     * хрупкими. Реализации могут быть независимы и несвязанными наследованием.
     */
    interface ClockProvider {

        /**
         * Возвращает текущий момент времени. Вызывается в main у обоих
         * провайдеров, чтобы показать одинаковый тип и разное поведение.
         */
        Instant now();
    }

    /**
     * Боевые часы системы. Единственное состояние контракта не хранит —
     * момент берётся у самой JVM.
     */
    static class SystemClockProvider implements ClockProvider {

        /**
         * Возвращает Instant.now(). Вызывается в main через переменную
         * live типа ClockProvider.
         */
        @Override
        public Instant now() {
            return Instant.now();
        }
    }

    /**
     * Тестовые часы с замороженным временем. Демонстрирует, что контракт
     * не хранит данные, а конкретная реализация хранит сколько угодно.
     */
    static class FixedClockProvider implements ClockProvider {

        private final Instant fixed;

        /**
         * Фиксирует «замороженный» момент времени. Вызывается в main
         * при создании тестового провайдера.
         */
        FixedClockProvider(Instant fixed) {
            this.fixed = fixed;
        }

        /**
         * Всегда возвращает один и тот же момент — благодаря этому тесты
         * детерминированы. Вызывается в main через переменную test
         * типа ClockProvider.
         */
        @Override
        public Instant now() {
            return fixed;
        }
    }

    /**
     * Точка входа. Демонстрирует все пять решений: общий код BaseEntity
     * (toString, id, equals, авто-id, реестр по контракту Identifiable),
     * стратегии CoursePolicy, обобщённый Formatter<T> вместе с renderAny,
     * подмену времени через ClockProvider и сводную таблицу решений,
     * которую печатает printDecision.
     */
    public static void main(String[] args) {

        Instant now = Instant.now();

        System.out.println("=== ДЕМОНСТРАЦИЯ BaseEntity (общий код в базовом классе) ===");
        Student s = new Student("STU-1", now, "Иван Иванов");
        Course c = new Course("CRS-101", now, "Основы Java");
        System.out.println("   " + s);
        System.out.println("   " + c);
        System.out.println("   id() работает через контракт Identifiable: " + s.id());

        Student s2 = new Student("STU-1", now, "Иван Иванов-копия");
        System.out.println("   s.equals(s2) = " + s.equals(s2)
                + "  <- реализация equals была написана ОДИН раз в базовом классе");
        System.out.println("   s.equals(c)  = " + s.equals(c)
                + "  <- защита от сравнения «всё со всем»");

        Student noId = new Student(null, now, "Без ID");
        System.out.println("   авто-id: " + noId.id());

        java.util.Map<String, Identifiable> registry = new java.util.HashMap<String, Identifiable>();
        registry.put(s.id(), s);
        registry.put(c.id(), c);
        System.out.println("   в реестре по id: " + registry.size() + " объектов,"
                + " типы элементов: Identifiable (конкретные классы неизвестны клиенту)");

        System.out.println();
        System.out.println("=== ДЕМОНСТРАЦИЯ CoursePolicy (стратегия) ===");
        Student applicant = new Student("STU-3", now, "Пётр Петров");
        Course[] catalog = {c, new Course("CRS-202", now, "Сети")};
        CoursePolicy[] policies = {new AgePolicy(18), new CourseLevelPolicy()};
        for (CoursePolicy policy : policies) {
            for (Course course : catalog) {
                System.out.println("   правило " + policy.name() + " на курс "
                        + course.id() + " = " + policy.isSatisfiedBy(applicant, course));
            }
        }
        System.out.println("   добавить новое правило = +1 класс, 0 правок в этом коде");

        System.out.println();
        System.out.println("=== ДЕМОНСТРАЦИЯ Formatter<T> (обобщения) ===");
        Formatter<Student> sf = new StudentTextFormatter();
        Formatter<Course> cf = new CourseTextFormatter();
        System.out.println("   " + sf.format(applicant));
        System.out.println("   " + cf.format(c));
        System.out.println("   renderAny(course, CourseTextFormatter) = "
                + renderAny(c, new CourseTextFormatter()));

        System.out.println();
        System.out.println("=== ДЕМОНСТРАЦИЯ ClockProvider (подмена времени) ===");
        ClockProvider live = new SystemClockProvider();
        System.out.println("   live.now()      = " + live.now());
        ClockProvider test = new FixedClockProvider(Instant.ofEpochMilli(0L));
        System.out.println("   test.now()      = " + test.now()
                + "  <- всегда одно и то же, тесты детерминированы");
        System.out.println("   тип переменных одинаковый: " + live.getClass().getSimpleName()
                + " / " + test.getClass().getSimpleName());

        System.out.println();
        System.out.println("=== СВОДКА РЕШЕНИЙ ===");
        printDecision("Identifiable",
                "interface",
                "носителей много, они не родственники; нужно только «имя»",
                "не сможем хранить состояние и общий код -> придётся BaseEntity");
        printDecision("CoursePolicy",
                "interface",
                "стратегия: поведение без состояния, иерархия открыта для внешних",
                "общий код правил придётся выносить в default-методы/декоратор");
        printDecision("BaseEntity",
                "abstract class",
                "общие ДАННЫЕ и общий КОД equals/hashCode/toString; отношение is-a",
                "только один родитель на класс; миграция ломает всех потомков");
        printDecision("Formatter",
                "interface + generics",
                "чистая функция; нужна типизация «форматтер для T»",
                "при потребности в состоянии -> abstract FormatterBase");
        printDecision("ClockProvider",
                "interface",
                "подменяемость времени в тестах; несколько независимых источников",
                "если времени не надо менять -> лишняя абстракция (но дешёвая)");
    }

    /**
     * Печатает одно решение сводной таблицы: понятие, выбранную форму,
     * мотивировку и цену ошибки. Вызывается пять раз из main в конце,
     * чтобы не дублировать System.out.println в главном методе.
     */
    private static void printDecision(String concept, String choice,
                                      String motivation, String cost) {
        System.out.println();
        System.out.println("   " + concept + "  ->  " + choice);
        System.out.println("      мотивировка: " + motivation);
        System.out.println("      цена ошибки: " + cost);
    }
}

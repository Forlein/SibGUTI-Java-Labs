import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Задание 9. «Политика допуска как стратегия».
 *
 * Показывает замену «политики в виде строки» (String policyName + здоровенный
 * if/switch в сервисе) на контракт EnrollmentPolicy и внедрение стратегии
 * конструктором. EnrollmentService не содержит ни instanceof, ни switch,
 * ни имён конкретных политик, поэтому новые правила добавляются новым классом
 * без единой правки сервиса.
 *
 * Запуск: javac 09_EnrollmentPolicyStrategy.java
 *         java  -cp . EnrollmentPolicyStrategy
 */
class EnrollmentPolicyStrategy {

    /**
     * Студент: неизменяемая модель кандидата. Множество сданных дисциплин
     * хранится как Set (без дубликатов) и отдаётся в unmodifiable-обёртке.
     */
    static class Student {

        private final String id;
        private final int age;
        private final Set<String> completedCourseCodes;
        private final int completedCredits;
        private final boolean resident;

        /**
         * Конструктор «по умолчанию»: не-резидент. Вызывается неявно
         * из полного конструктора, когда флаг резидентства не передан.
         */
        Student(String id, int age, Set<String> completedCourseCodes, int completedCredits) {
            this(id, age, completedCourseCodes, completedCredits, false);
        }

        /**
         * Полный конструктор: копирует входное множество в LinkedHashSet,
         * защищая объект от внешнего изменения. Вызывается из main
         * при создании кандидатов young и ready.
         */
        Student(String id, int age, Set<String> completedCourseCodes, int completedCredits,
                boolean resident) {
            this.id = id;
            this.age = age;
            this.completedCourseCodes = new LinkedHashSet<String>(completedCourseCodes);
            this.completedCredits = completedCredits;
            this.resident = resident;
        }

        /**
         * Возвращает идентификатор кандидата. Используется сервисом
         * для записи в журнал зачислений.
         */
        String getId() {
            return id;
        }

        /**
         * Возвращает возраст. Используется стратегией AgePolicy
         * при вынесении решения.
         */
        int getAge() {
            return age;
        }

        /**
         * Сообщает, является ли кандидат резидентом. Используется стратегией
         * ResidencyPolicy для безусловного зачисления.
         */
        boolean isResident() {
            return resident;
        }

        /**
         * Возвращает множество сданных дисциплин в неизменяемой обёртке.
         * Используется стратегией PrerequisitesPolicy, вызывающей containsAll(...).
         */
        Set<String> getCompletedCourseCodes() {
            return java.util.Collections.unmodifiableSet(completedCourseCodes);
        }

        /**
         * Возвращает число сданных кредитов. Зарезервировано для правил
         * по кредитам; в текущих стратегиях не вызывается.
         */
        int getCompletedCredits() {
            return completedCredits;
        }

        /**
         * Переопределяет Object.toString() описанием кандидата.
         * Используется в main при печати состава тестовых данных.
         */
        @Override
        public String toString() {
            return "студент " + id + " (возраст " + age + ", кредитов сдано "
                    + completedCredits + ")";
        }
    }

    /**
     * Курс: неизменяемая модель дисциплины с требованиями к поступлению
     * и вместимостью. Требования отдаются в unmodifiable-обёртке.
     */
    static class Course {

        private final String code;
        private final String title;
        private final int requiredCredits;
        private final Set<String> prerequisites;
        private final int capacity;

        /**
         * Создаёт курс и копирует множество пререквизитов в LinkedHashSet.
         * Вызывается из main при формировании двух тестовых курсов.
         */
        Course(String code, String title, int requiredCredits,
               Set<String> prerequisites, int capacity) {
            this.code = code;
            this.title = title;
            this.requiredCredits = requiredCredits;
            this.prerequisites = new LinkedHashSet<String>(prerequisites);
            this.capacity = capacity;
        }

        /**
         * Возвращает шифр курса. Используется сервисом для записи
         * в журнал зачислений.
         */
        String getCode() {
            return code;
        }

        /**
         * Возвращает название курса. Используется в toString()
         * при печати состава тестовых данных.
         */
        String getTitle() {
            return title;
        }

        /**
         * Возвращает число требуемых кредитов. Зарезервировано для правил
         * по кредитам; в текущих стратегиях не вызывается.
         */
        int getRequiredCredits() {
            return requiredCredits;
        }

        /**
         * Возвращает множество пререквизитов в неизменяемой обёртке.
         * Используется стратегией PrerequisitesPolicy.
         */
        Set<String> getPrerequisites() {
            return java.util.Collections.unmodifiableSet(prerequisites);
        }

        /**
         * Возвращает вместимость. Используется стратегией CapacityPolicy
         * вместе с числом уже зачисленных, переданным сервисом.
         */
        int getCapacity() {
            return capacity;
        }

        /**
         * Переопределяет Object.toString() сводкой требований.
         * Используется в main при печати курса.
         */
        @Override
        public String toString() {
            return "курс " + code + " '" + title + "' (нужно кредитов: " + requiredCredits
                    + ", пререквизиты: " + prerequisites + ", мест: " + capacity + ")";
        }
    }

    /**
     * Результат проверки политики. Типизированное перечисление нужно потому,
     * что исходов три, а не два: boolean не смог бы отличить отказ
     * от постановки в лист ожидания.
     */
    enum EnrollmentDecision {

        /** Кандидат зачислен. */
        ENROLLED,

        /** Кандидату отказано. */
        REJECTED,

        /** Кандидат поставлен в лист ожидания. */
        WAITLISTED
    }

    /**
     * Контракт стратегии допуска. Открытый интерфейс: правило не знает,
     * как его вызывают, не хранит глобального состояния, а всё нужное
     * для решения получает аргументами.
     */
    interface EnrollmentPolicy {

        /**
         * Возвращает человекочитаемое имя правила для логов и отчётов.
         * Вызывается сервисом в policyName() и клиентом в main при печати.
         */
        String name();

        /**
         * Принимает решение о допуске. Возвращает EnrollmentDecision,
         * а не boolean, поскольку различает три исхода. Вызывается
         * сервисом EnrollmentService.enroll(...), а также внутри
         * составных политик AnyOfPolicy и ResidencyPolicy.
         */
        EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled);
    }

    /**
     * Стратегия «минимальный возраст». Единственное состояние — настройка
     * minAge, то есть данные, а не отдельный подкласс.
     */
    static class AgePolicy implements EnrollmentPolicy {

        private final int minAge;

        /**
         * Задаёт минимальный возраст. Вызывается в main при сборке
         * массива политик и внутри составной политики AnyOfPolicy.
         */
        AgePolicy(int minAge) {
            this.minAge = minAge;
        }

        /**
         * Возвращает имя вместе с настройкой. Вызывается сервисом
         * и клиентом при печати результата.
         */
        @Override
        public String name() {
            return "AGE(" + minAge + "+)";
        }

        /**
         * Сравнивает возраст кандидата с minAge и возвращает ENROLLED либо
         * REJECTED. Вызывается сервисом EnrollmentService.enroll(...).
         */
        @Override
        public EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled) {
            if (student.getAge() >= minAge) {
                return EnrollmentDecision.ENROLLED;
            }
            return EnrollmentDecision.REJECTED;
        }
    }

    /**
     * Стратегия «все пререквизиты сданы». Состояния нет вовсе —
     * вся проверка на основе данных кандидата и курса.
     */
    static class PrerequisitesPolicy implements EnrollmentPolicy {

        /**
         * Возвращает имя "PREREQUISITES". Вызывается сервисом
         * и клиентом при печати результата.
         */
        @Override
        public String name() {
            return "PREREQUISITES";
        }

        /**
         * Проверяет containsAll(...) по множествам сданных дисциплин
         * и пререквизитов. Вызывается сервисом напрямую и через
         * делегирование из ResidencyPolicy для не-резидентов.
         */
        @Override
        public EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled) {
            boolean hasAll = student.getCompletedCourseCodes().containsAll(course.getPrerequisites());
            return hasAll ? EnrollmentDecision.ENROLLED : EnrollmentDecision.REJECTED;
        }
    }

    /**
     * Стратегия «вместимость». Только она использует число уже зачисленных,
     * и именно она различает отказ и лист ожидания.
     */
    static class CapacityPolicy implements EnrollmentPolicy {

        /**
         * Возвращает имя "CAPACITY". Вызывается сервисом
         * и клиентом при печати результата.
         */
        @Override
        public String name() {
            return "CAPACITY";
        }

        /**
         * Сравнивает число занятых мест с вместимостью курса; при нехватке
         * мест возвращает WAITLISTED, а не REJECTED, потому что различие
         * исходов живёт в правиле, а не в сервисе. Вызывается сервисом
         * и внутри составных политик.
         */
        @Override
        public EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled) {
            if (alreadyEnrolled < course.getCapacity()) {
                return EnrollmentDecision.ENROLLED;
            }
            return EnrollmentDecision.WAITLISTED;
        }
    }

    /**
     * Сервис зачисления: принимает стратегию конструктором и работает
     * с абстракцией EnrollmentPolicy. Ни одного упоминания конкретных
     * политик, поэтому новые правила не требуют правок этого класса.
     */
    static class EnrollmentService {

        private final EnrollmentPolicy policy;
        private final List<String> enrolled = new ArrayList<String>();

        /**
         * Внедряет стратегию снаружи и проверяет, что она задана.
         * Вызывается из main для каждого эксперимента: для каждой политики
         * массива, для составной политики и для новой ResidencyPolicy.
         */
        EnrollmentService(EnrollmentPolicy policy) {
            if (policy == null) {
                throw new IllegalArgumentException("политика обязательна");
            }
            this.policy = policy;
        }

        /**
         * Единственный публичный метод сервиса: спрашивает у стратегии
         * решение и при ENROLLED пишет строку в журнал. Одинаков для ЛЮБЫХ
         * политик. Вызывается из main во всех экспериментах.
         */
        public EnrollmentDecision enroll(Student student, Course course) {
            EnrollmentDecision decision = policy.evaluate(student, course, enrolled.size());

            if (decision == EnrollmentDecision.ENROLLED) {
                enrolled.add(student.getId() + " -> " + course.getCode());
            }
            return decision;
        }

        /**
         * Возвращает число зачисленных. Используется сервисом как
         * аргумент alreadyEnrolled для стратегии вместимости
         * и вызывается из main для печати занятых мест.
         */
        public int getEnrolledCount() {
            return enrolled.size();
        }

        /**
         * Возвращает имя внедрённой политики через контракт.
         * Вызывается из main для отчёта по составной политике,
         * чтобы показать, что сервис не знает конкретной реализации.
         */
        public String policyName() {
            return policy.name();
        }

        /**
         * Возвращает копию журнала зачислений. Вызывается из main
         * для печати содержимого журнала.
         */
        public List<String> getJournal() {
            return new ArrayList<String>(enrolled);
        }
    }

    /**
     * Составная стратегия «любая из вложенных». Не знает ни про возраст,
     * ни про пререквизиты: работает только с EnrollmentPolicy, поэтому
     * её состав меняется произвольно, а сам класс — никогда.
     */
    static class AnyOfPolicy implements EnrollmentPolicy {

        private final List<EnrollmentPolicy> policies = new ArrayList<EnrollmentPolicy>();
        private final String name;

        /**
         * Принимает имя для отчёта и произвольное число вложенных стратегий.
         * Вызывается из main для составной политики «возраст + пререквизиты +
         * вместимость».
         */
        AnyOfPolicy(String name, EnrollmentPolicy... items) {
            this.name = name;
            for (EnrollmentPolicy p : items) {
                policies.add(p);
            }
        }

        /**
         * Возвращает заданное имя составной стратегии. Вызывается сервисом
         * в policyName() и клиентом при печати.
         */
        @Override
        public String name() {
            return name;
        }

        /**
         * Опрашивает все вложенные стратегии и возвращает самое строгое
         * решение, определяя строгость через rank(...). Вызывается сервисом
         * напрямую либо другими составными стратегиями.
         */
        @Override
        public EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled) {
            EnrollmentDecision worst = EnrollmentDecision.REJECTED;
            for (EnrollmentPolicy p : policies) {
                EnrollmentDecision d = p.evaluate(student, course, alreadyEnrolled);
                if (rank(d) > rank(worst)) {
                    worst = d;
                }
            }
            return worst;
        }

        /**
         * Ранжирует исходы: чем больше значение, тем строже решение
         * (ENROLLED — 0, WAITLISTED — 1, REJECTED — 2). Используется
         * только внутри evaluate(...) этой же стратегии.
         */
        private int rank(EnrollmentDecision d) {
            if (d == EnrollmentDecision.ENROLLED) {
                return 0;
            }
            if (d == EnrollmentDecision.WAITLISTED) {
                return 1;
            }
            return 2;
        }
    }

    /**
     * Новая стратегия с признаком декоратора: резидентам всегда да,
     * остальным решает другая стратегия, переданная конструктором.
     * Класс EnrollmentService при этом не меняется ни одной строкой.
     */
    static class ResidencyPolicy implements EnrollmentPolicy {

        private final EnrollmentPolicy forNonResidents;

        /**
         * Задаёт стратегию для не-резидентов. Вызывается из main
         * для новой политики, оборачивая правило пререквизитов.
         */
        ResidencyPolicy(EnrollmentPolicy forNonResidents) {
            this.forNonResidents = forNonResidents;
        }

        /**
         * Возвращает имя "RESIDENCY". Вызывается сервисом и клиентом
         * при печати результата.
         */
        @Override
        public String name() {
            return "RESIDENCY";
        }

        /**
         * Резиденту возвращает ENROLLED, иначе делегирует оценку
         * вложенной стратегии. Вызывается сервисом EnrollmentService.enroll(...).
         */
        @Override
        public EnrollmentDecision evaluate(Student student, Course course, int alreadyEnrolled) {
            if (student.isResident()) {
                return EnrollmentDecision.ENROLLED;
            }
            return forNonResidents.evaluate(student, course, alreadyEnrolled);
        }
    }

    /**
     * Точка входа. Прогоняет один сервис со всеми тремя базовыми политиками,
     * показывает действие правила вместимости, собирает составную стратегию,
     * добавляет новую политику ResidencyPolicy (не меняя сервис),
     * демонстрирует чистоту сервиса на уровне исходника и перечисляет
     * случаи, когда стратегия не нужна.
     */
    public static void main(String[] args) {

        Course javaCourse = new Course("CRS-101", "Основы Java", 0,
                new LinkedHashSet<String>(), 2);
        Course netCourse = new Course("CRS-202", "Сетевые технологии", 6,
                new LinkedHashSet<String>(java.util.Arrays.asList("CRS-101")), 1);

        Student young = new Student("STU-1", 17,
                new LinkedHashSet<String>(), 0, false);
        Student ready = new Student("STU-2", 20,
                new LinkedHashSet<String>(java.util.Arrays.asList("CRS-101")), 6, true);

        System.out.println("=== 1. Разные политики для одного и того же сервиса ===");
        System.out.println("   студент: " + young);
        System.out.println("   курс:    " + javaCourse);

        EnrollmentPolicy[] policies = {
                new AgePolicy(18),
                new PrerequisitesPolicy(),
                new CapacityPolicy()
        };

        for (EnrollmentPolicy policy : policies) {
            EnrollmentService service = new EnrollmentService(policy);
            EnrollmentDecision d = service.enroll(young, javaCourse);
            System.out.println("   политика " + pad(policy.name()) + " -> " + d);
        }

        System.out.println();
        System.out.println("=== 2. Тот же сервис, другой студент ===");
        for (EnrollmentPolicy policy : policies) {
            EnrollmentService service = new EnrollmentService(policy);
            EnrollmentDecision d = service.enroll(ready, javaCourse);
            System.out.println("   политика " + pad(policy.name()) + " -> " + d);
        }

        System.out.println();
        System.out.println("=== 3. Правило вместимости в действии (мест = 1) ===");
        EnrollmentService capacityService = new EnrollmentService(new CapacityPolicy());
        System.out.println("   студент ready  -> " + capacityService.enroll(ready, netCourse)
                + " (занято мест: " + capacityService.getEnrolledCount() + ")");
        System.out.println("   студент young  -> " + capacityService.enroll(young, netCourse)
                + " (занято мест: " + capacityService.getEnrolledCount() + ")");
        System.out.println("   журнал: " + capacityService.getJournal());

        System.out.println();
        System.out.println("=== 4. КОМПОЗИЦИЯ: правило из правил (тот же контракт!) ===");
        EnrollmentPolicy composite = new AnyOfPolicy("STRICT(age+prereq+place)",
                new AgePolicy(18),
                new PrerequisitesPolicy(),
                new CapacityPolicy());
        for (Student s : new Student[]{young, ready}) {
            EnrollmentService service = new EnrollmentService(composite);
            System.out.println("   " + s.getId() + " -> " + service.enroll(s, netCourse)
                    + "   политика: " + service.policyName());
        }
        System.out.println("   ВНИМАНИЕ: AnyOfPolicy реализует EnrollmentPolicy,");
        System.out.println("   поэтому сервис о нём ничего не знает. Это и есть полиморфизм.");

        System.out.println();
        System.out.println("=== 5. НОВАЯ ПОЛИТИКА: EnrollmentService НЕ ИЗМЕНЁН ===");
        EnrollmentPolicy residency = new ResidencyPolicy(new PrerequisitesPolicy());
        System.out.println("   политика " + pad(residency.name()) + ":");
        System.out.println("      ready (резидент)  -> "
                + new EnrollmentService(residency).enroll(ready, netCourse)
                + "   (резиденту всегда да)");
        System.out.println("      young (не резидент)-> "
                + new EnrollmentService(residency).enroll(young, netCourse)
                + "   (делегировало правилу пререквизитов: их нет)");

        System.out.println();
        System.out.println("=== 6. СЕРВИС НЕ ЗНАЕТ ПРО КОНКРЕТНЫЕ ПОЛИТИКИ ===");
        System.out.println("   EnrollmentService использует ровно один тип: EnrollmentPolicy.");
        System.out.println("   Стратегии создаются только в точке сборки (main / DI-контейнер).");

        System.out.println();
        System.out.println("=== 7. КОГДА СТРАТЕГИЯ НЕ НУЖНА ===");
        System.out.println("   Стратегия оправдана, если:");
        System.out.println("     • правил будет несколько;");
        System.out.println("     • набор правил меняется со временем;");
        System.out.println("     • одно и то же правило используется разными клиентами;");
        System.out.println("     • правила нужно тестировать по отдельности.");
        System.out.println("   Если правило ровно одно и не изменится — стратегия это");
        System.out.println("   лишняя абстракция (один класс-обёртка и лишняя косвенность).");
    }

    /**
     * Дополняет строку пробелами до 20 символов, чтобы колонки
     * с именами политик выравнивались в выводе. Вызывается из main
     * при печати результатов в шагах 1, 4 и 5.
     */
    private static String pad(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < 20) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
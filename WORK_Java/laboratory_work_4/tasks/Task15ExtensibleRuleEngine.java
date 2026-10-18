package tasks;

import common.ClockProvider;
import common.Course;
import common.Student;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Задание 15. Расширяемый движок правил (CourseHub enrollment engine).
 * Правила возвращают типизированный результат, могут быть открытыми стратегиями
 * или закрытой sealed-иерархией, композиция правил поддерживает allOf и anyOf,
 * а сервис не проверяет конкретные классы.
 * Требуется диаграмма зависимостей, минимум пять реализаций, демонстрация dynamic dispatch
 * и записка: где наследование уместно, где выбрана композиция и какие гарантии даёт sealed-граница.
 */
public class Task15ExtensibleRuleEngine {

    // === 1. Закрытая sealed-иерархия вердикта правила ===
    sealed interface RuleVerdict permits Pass, Fail, Defer {
        String explain();
    }

    record Pass(String note) implements RuleVerdict {
        @Override
        public String explain() {
            return "PASS (" + note + ")";
        }
    }

    record Fail(String reason) implements RuleVerdict {
        @Override
        public String explain() {
            return "FAIL (" + reason + ")";
        }
    }

    record Defer(String reason) implements RuleVerdict {
        @Override
        public String explain() {
            return "DEFER (" + reason + ")";
        }
    }

    // === 2. Закрытая sealed-иерархия итогового решения сервиса ===
    sealed interface EnrollmentDecision permits AcceptedDecision, RejectedDecision, WaitListedDecision {
        String describe();
    }

    record AcceptedDecision(String studentId, String courseCode) implements EnrollmentDecision {
        @Override
        public String describe() {
            return "ACCEPTED: " + studentId + " -> " + courseCode;
        }
    }

    record RejectedDecision(String studentId, String reason) implements EnrollmentDecision {
        @Override
        public String describe() {
            return "REJECTED: " + studentId + " [" + reason + "]";
        }
    }

    record WaitListedDecision(String studentId, int position, String note) implements EnrollmentDecision {
        @Override
        public String describe() {
            return "WAITLISTED: " + studentId + " (#" + position + ", " + note + ")";
        }
    }

    // === 3. Внешний порт и контекст зачисления ===
    interface EnrollmentPortal {
        int seatsTaken(String courseCode);
        boolean hasApprovedStudyPlan(String studentId);
        void recordEnrollment(String studentId, String courseCode);
    }

    static final class InMemoryPortal implements EnrollmentPortal {
        private final Map<String, List<String>> enrolledByCourse = new HashMap<>();
        private final Set<String> studentsWithoutPlan = new HashSet<>();

        void markWithoutPlan(String studentId) {
            studentsWithoutPlan.add(studentId);
        }

        @Override
        public int seatsTaken(String courseCode) {
            return enrolledByCourse.getOrDefault(courseCode, List.of()).size();
        }

        @Override
        public boolean hasApprovedStudyPlan(String studentId) {
            return !studentsWithoutPlan.contains(studentId);
        }

        @Override
        public void recordEnrollment(String studentId, String courseCode) {
            enrolledByCourse.computeIfAbsent(courseCode, k -> new ArrayList<>()).add(studentId);
        }
    }

    record EnrollmentContext(
            Student student,
            Course course,
            EnrollmentPortal portal,
            ClockProvider clock
    ) {}

    // === 4. Открытый интерфейс стратегии правила с поддержкой композиции allOf / anyOf ===
    interface EnrollmentRule {
        String name();
        RuleVerdict evaluate(EnrollmentContext ctx);

        static EnrollmentRule allOf(String name, EnrollmentRule... rules) {
            return new AllOfRule(name, List.of(rules));
        }

        static EnrollmentRule anyOf(String name, EnrollmentRule... rules) {
            return new AnyOfRule(name, List.of(rules));
        }
    }

    // Реализация 1: Возрастной порог
    static final class MinAgeRule implements EnrollmentRule {
        private final int minAge;

        MinAgeRule(int minAge) {
            this.minAge = minAge;
        }

        @Override
        public String name() {
            return "MinAge(" + minAge + ")";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            int age = ctx.student().getAge();
            return age >= minAge
                    ? new Pass("возраст " + age + " >= " + minAge)
                    : new Fail("возраст " + age + " < " + minAge);
        }
    }

    // Реализация 2: Проверка пререквизитов
    static final class PrerequisitesRule implements EnrollmentRule {
        @Override
        public String name() {
            return "Prerequisites";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            Set<String> required = ctx.course().getPrerequisites();
            Set<String> completed = ctx.student().getCompletedCourses();
            for (String req : required) {
                if (!completed.contains(req)) {
                    return new Fail("не сдан пререквизит " + req);
                }
            }
            return new Pass("все пререквизиты сданы " + required);
        }
    }

    // Реализация 3: Проверка вместимости (при нехватке мест возвращает Defer -> лист ожидания)
    static final class CapacityRule implements EnrollmentRule {
        @Override
        public String name() {
            return "Capacity";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            int taken = ctx.portal().seatsTaken(ctx.course().getCode());
            int cap = ctx.course().getCapacity();
            return taken < cap
                    ? new Pass("свободно мест: " + (cap - taken) + " из " + cap)
                    : new Defer("все места заняты (" + taken + "/" + cap + ")");
        }
    }

    // Реализация 4: Наличие утверждённого индивидуального учебного плана
    static final class StudyPlanRule implements EnrollmentRule {
        @Override
        public String name() {
            return "StudyPlan";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            return ctx.portal().hasApprovedStudyPlan(ctx.student().id())
                    ? new Pass("учебный план утверждён")
                    : new Fail("нет утверждённого учебного плана");
        }
    }

    // Реализация 5: Защита от дублирующей записи на тот же курс
    static final class DuplicateEnrollmentRule implements EnrollmentRule {
        private final Set<String> seenPairs = new HashSet<>();

        @Override
        public String name() {
            return "NoDuplicate";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            String key = ctx.student().id() + "@" + ctx.course().getCode();
            if (!seenPairs.add(key)) {
                return new Fail("повторная подача заявки на " + ctx.course().getCode());
            }
            return new Pass("первичная заявка");
        }
    }

    // Реализация 6: Проверка минимального числа кредитов
    static final class CreditsQuotaRule implements EnrollmentRule {
        private final int requiredCredits;

        CreditsQuotaRule(int requiredCredits) {
            this.requiredCredits = requiredCredits;
        }

        @Override
        public String name() {
            return "CreditsQuota(" + requiredCredits + ")";
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            return ctx.student().getCompletedCredits() >= requiredCredits
                    ? new Pass("кредитов достаточно: " + ctx.student().getCompletedCredits())
                    : new Fail("недостаточно кредитов: " + ctx.student().getCompletedCredits() + " < " + requiredCredits);
        }
    }

    // Реализация 7 (Композиция И): AllOfRule
    static final class AllOfRule implements EnrollmentRule {
        private final String name;
        private final List<EnrollmentRule> children;

        AllOfRule(String name, List<EnrollmentRule> children) {
            this.name = name;
            this.children = List.copyOf(children);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            Defer firstDefer = null;
            for (EnrollmentRule child : children) {
                RuleVerdict v = child.evaluate(ctx);
                switch (v) {
                    case Fail f -> {
                        return new Fail(child.name() + ": " + f.reason());
                    }
                    case Defer d -> {
                        if (firstDefer == null) {
                            firstDefer = new Defer(child.name() + ": " + d.reason());
                        }
                    }
                    case Pass ignored -> {}
                }
            }
            return firstDefer != null ? firstDefer : new Pass("все правила в " + name + " пройдены");
        }
    }

    // Реализация 8 (Композиция ИЛИ): AnyOfRule
    static final class AnyOfRule implements EnrollmentRule {
        private final String name;
        private final List<EnrollmentRule> children;

        AnyOfRule(String name, List<EnrollmentRule> children) {
            this.name = name;
            this.children = List.copyOf(children);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public RuleVerdict evaluate(EnrollmentContext ctx) {
            Defer firstDefer = null;
            List<String> reasons = new ArrayList<>();
            for (EnrollmentRule child : children) {
                RuleVerdict v = child.evaluate(ctx);
                switch (v) {
                    case Pass p -> {
                        return new Pass(child.name() + ": " + p.note());
                    }
                    case Defer d -> {
                        if (firstDefer == null) {
                            firstDefer = new Defer(child.name() + ": " + d.reason());
                        }
                    }
                    case Fail f -> reasons.add(child.name() + "(" + f.reason() + ")");
                }
            }
            return firstDefer != null ? firstDefer : new Fail("ни одно правило не выполнено: " + String.join(", ", reasons));
        }
    }

    // === 5. Сервис CourseHub: зависит ТОЛЬКО от EnrollmentRule, RuleVerdict, EnrollmentDecision ===
    static final class CourseHubEnrollmentService {
        private final List<EnrollmentRule> rules;
        private final EnrollmentPortal portal;
        private final ClockProvider clock;
        private int waitListSeq = 0;

        CourseHubEnrollmentService(List<EnrollmentRule> rules, EnrollmentPortal portal, ClockProvider clock) {
            this.rules = List.copyOf(rules);
            this.portal = portal;
            this.clock = clock;
        }

        public EnrollmentDecision enroll(Student student, Course course) {
            EnrollmentContext ctx = new EnrollmentContext(student, course, portal, clock);
            String deferReason = null;

            for (EnrollmentRule rule : rules) {
                // Dynamic dispatch: вызов rule.evaluate(ctx) полиморфно уходит в конкретную реализацию или композит
                RuleVerdict verdict = rule.evaluate(ctx);
                System.out.println("    -> Правило [" + rule.name() + "] вернуло: " + verdict.explain());

                // Exhaustive switch по закрытой sealed-границе RuleVerdict (без проверки классов правил!)
                switch (verdict) {
                    case Fail f -> {
                        return new RejectedDecision(student.id(), rule.name() + " -> " + f.reason());
                    }
                    case Defer d -> {
                        if (deferReason == null) {
                            deferReason = rule.name() + " -> " + d.reason();
                        }
                    }
                    case Pass ignored -> {}
                }
            }

            if (deferReason != null) {
                waitListSeq++;
                return new WaitListedDecision(student.id(), waitListSeq, deferReason);
            }

            portal.recordEnrollment(student.id(), course.getCode());
            return new AcceptedDecision(student.id(), course.getCode());
        }
    }

    public static void main(String[] args) {
        printDependencyDiagram();

        InMemoryPortal portal = new InMemoryPortal();
        ClockProvider clock = () -> Instant.parse("2026-10-18T09:00:00Z");

        Course netCourse = new Course("NET-301", "Компьютерные сети", 5, Set.of("CS-101"), 1);

        Student alice = new Student("STU-1", "Алиса", 20, Set.of("CS-101"), 30, true);
        Student bobYoung = new Student("STU-2", "Боб", 16, Set.of("CS-101"), 30, true);
        Student charlieWait = new Student("STU-3", "Чарли", 21, Set.of("CS-101"), 10, true);
        Student daveNoPrereq = new Student("STU-4", "Дэйв", 22, Set.of(), 5, true);

        // Композиция правил с использованием allOf и anyOf
        EnrollmentRule compositePipeline = EnrollmentRule.allOf(
                "MainPipeline",
                new MinAgeRule(18),
                EnrollmentRule.anyOf(
                        "PrereqOrCredits",
                        new PrerequisitesRule(),
                        new CreditsQuotaRule(25)
                ),
                new StudyPlanRule(),
                new DuplicateEnrollmentRule(),
                new CapacityRule()
        );

        CourseHubEnrollmentService engine = new CourseHubEnrollmentService(
                List.of(compositePipeline), portal, clock
        );

        System.out.println("\n=== 2. Демонстрация Dynamic Dispatch и работы правил ===");
        runScenario(engine, alice, netCourse, "Сценарий 1: Алиса проходит все правила и занимает 1-е место");
        runScenario(engine, bobYoung, netCourse, "Сценарий 2: Боб (16 лет) отклоняется правилом MinAgeRule");
        runScenario(engine, charlieWait, netCourse, "Сценарий 3: Чарли проходит проверки, но мест нет -> WaitListed");
        runScenario(engine, daveNoPrereq, netCourse, "Сценарий 4: Дэйв не имеет ни пререквизита, ни 25 кредитов -> Fail в AnyOfRule");
        runScenario(engine, alice, netCourse, "Сценарий 5: Повторная запись Алисы -> отказ в DuplicateEnrollmentRule");

        printArchitecturalNote();
    }

    private static void runScenario(CourseHubEnrollmentService engine, Student s, Course c, String title) {
        System.out.println("\n--- " + title + " ---");
        EnrollmentDecision decision = engine.enroll(s, c);
        String formatted = switch (decision) {
            case AcceptedDecision a -> "[OK]   " + a.describe();
            case RejectedDecision r -> "[FAIL] " + r.describe();
            case WaitListedDecision w -> "[WAIT] " + w.describe();
        };
        System.out.println("  ИТОГ: " + formatted);
    }

    private static void printDependencyDiagram() {
        System.out.println("=== 1. Диаграмма зависимостей CourseHub Enrollment Engine ===");
        System.out.println("""
                  +-------------------------------------------+
                  |        CourseHubEnrollmentService         |
                  +-------------------------------------------+
                       |                  |                |
                       | вызывает         | обрабатывает   | возвращает
                       v                  v                v
              +----------------+   +-------------+   +--------------------+
              | EnrollmentRule |   | RuleVerdict |   | EnrollmentDecision |
              |  (interface)   |   |  (sealed)   |   |      (sealed)      |
              +----------------+   +-------------+   +--------------------+
                       ^              ^   ^   ^         ^       ^       ^
                       | implements   |   |   |         |       |       |
            +----------+----------+  Pass Fail Defer  Accepted Rejected WaitListed
            |          |          |
          MinAgeRule  ...   AllOfRule / AnyOfRule (содержат List<EnrollmentRule>)
          (6 листовых)       (рекурсивная композиция)""");
    }

    private static void printArchitecturalNote() {
        System.out.println("\n=== 3. Архитектурная записка ===");
        System.out.println("1) Где наследование уместно:");
        System.out.println("   - В базовых сущностях предметной области (Student, Course extends BaseEntity) для общих полей id/createdAt.");
        System.out.println("   - В закрытых иерархиях результатов (RuleVerdict, EnrollmentDecision), где подтипы образуют строгий АТД.");
        System.out.println("2) Где выбрана композиция:");
        System.out.println("   - В сборке пайплайна CourseHubEnrollmentService (хранит List<EnrollmentRule>).");
        System.out.println("   - В комбинаторах AllOfRule и AnyOfRule (паттерн Composite: правило содержит список других правил).");
        System.out.println("   - В передаче зависимостей EnrollmentPortal и ClockProvider через EnrollmentContext.");
        System.out.println("3) Какие гарантии даёт sealed-граница:");
        System.out.println("   - EnrollmentRule оставлен ОТКРЫТЫМ интерфейсом: добавлять новые правила можно без изменения движка.");
        System.out.println("   - RuleVerdict и EnrollmentDecision закрыты через sealed/permits: компилятор гарантирует полноту");
        System.out.println("     pattern-matching switch без ветки default и защищает движок от неизвестных подтипов результата.");
    }
}

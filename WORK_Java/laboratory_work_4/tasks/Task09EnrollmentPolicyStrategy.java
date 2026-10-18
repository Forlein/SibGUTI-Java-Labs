package tasks;

import common.Course;
import common.Student;
import java.util.Set;

/**
 * Задание 9. Политика допуска как стратегия.
 * Объявите EnrollmentPolicy и реализации по возрасту, prerequisites и вместимости.
 * Клиент принимает политику конструктором.
 * Добавление новой политики не меняет EnrollmentService.
 */
public class Task09EnrollmentPolicyStrategy {

    interface EnrollmentPolicy {
        String name();
        boolean canEnroll(Student student, Course course, int currentEnrolled);
    }

    static class AgeEnrollmentPolicy implements EnrollmentPolicy {
        private final int minAge;

        AgeEnrollmentPolicy(int minAge) {
            this.minAge = minAge;
        }

        @Override
        public String name() {
            return "Age >= " + minAge;
        }

        @Override
        public boolean canEnroll(Student student, Course course, int currentEnrolled) {
            return student.getAge() >= minAge;
        }
    }

    static class PrerequisitesEnrollmentPolicy implements EnrollmentPolicy {
        @Override
        public String name() {
            return "Prerequisites";
        }

        @Override
        public boolean canEnroll(Student student, Course course, int currentEnrolled) {
            return student.getCompletedCourses().containsAll(course.getPrerequisites());
        }
    }

    static class CapacityEnrollmentPolicy implements EnrollmentPolicy {
        @Override
        public String name() {
            return "Capacity";
        }

        @Override
        public boolean canEnroll(Student student, Course course, int currentEnrolled) {
            return currentEnrolled < course.getCapacity();
        }
    }

    /**
     * Новая четвёртая политика (по минимальному числу кредитов), добавленная без правок EnrollmentService.
     */
    static class MinCreditsEnrollmentPolicy implements EnrollmentPolicy {
        private final int minCredits;

        MinCreditsEnrollmentPolicy(int minCredits) {
            this.minCredits = minCredits;
        }

        @Override
        public String name() {
            return "MinCredits >= " + minCredits;
        }

        @Override
        public boolean canEnroll(Student student, Course course, int currentEnrolled) {
            return student.getCompletedCredits() >= minCredits;
        }
    }

    /**
     * Клиентский сервис зачисления: принимает стратегию через конструктор.
     */
    static class EnrollmentService {
        private final EnrollmentPolicy policy;

        EnrollmentService(EnrollmentPolicy policy) {
            this.policy = policy;
        }

        public boolean tryEnroll(Student student, Course course, int currentEnrolled) {
            boolean allowed = policy.canEnroll(student, course, currentEnrolled);
            System.out.println("  [" + policy.name() + "] Студент " + student.getFullName()
                    + " -> " + (allowed ? "ДОПУЩЕН" : "ОТКЛОНЕН"));
            return allowed;
        }
    }

    public static void main(String[] args) {
        Course course = new Course("JAVA-202", "Продвинутая Java", 6, Set.of("JAVA-101"), 2);
        Student readyStudent = new Student("S-1", "Иван Петров", 20, Set.of("JAVA-101"), 12, true);
        Student juniorStudent = new Student("S-2", "Олег Сидоров", 16, Set.of(), 0, true);

        EnrollmentPolicy[] policies = {
                new AgeEnrollmentPolicy(18),
                new PrerequisitesEnrollmentPolicy(),
                new CapacityEnrollmentPolicy(),
                new MinCreditsEnrollmentPolicy(10) // новая политика, EnrollmentService не менялся
        };

        System.out.println("=== Проверка политик допуска через неизменный EnrollmentService ===");
        for (EnrollmentPolicy policy : policies) {
            EnrollmentService service = new EnrollmentService(policy);
            service.tryEnroll(readyStudent, course, 1);
            service.tryEnroll(juniorStudent, course, 2);
        }
    }
}

package tasks;

import common.BaseEntity;
import common.ClockProvider;
import common.Course;
import common.Identifiable;
import common.Student;
import java.time.Instant;

/**
 * Задание 5. Интерфейс или абстрактный класс.
 * Для понятий Identifiable, CoursePolicy, BaseEntity, Formatter, ClockProvider
 * выберите interface, abstract class, обычный class или отсутствие отдельного типа.
 * Для каждого решения запишите мотивировку и цену изменения.
 */
public class Task05InterfaceOrAbstractClass {

    // CoursePolicy — открытый контракт стратегии без общего состояния -> interface
    interface CoursePolicy {
        boolean isAllowed(Student student, Course course);
    }

    // Formatter<T> — функциональный обобщённый контракт без состояния -> interface
    interface Formatter<T> {
        String format(T item);
    }

    public static void main(String[] args) {
        Student student = new Student("STU-10", "Алексей Смирнов", 20);
        Course course = new Course("CS-101", "Алгоритмы и структуры данных", 5);

        Identifiable idRef = student;
        BaseEntity entityRef = course;
        CoursePolicy agePolicy = (s, c) -> s.getAge() >= 18;
        Formatter<Course> formatter = c -> c.getCode() + ": " + c.getTitle();
        ClockProvider fixedClock = () -> Instant.parse("2026-10-18T12:00:00Z");

        System.out.println("=== 1. Проверка выбранных типов в работе ===");
        System.out.println("  Identifiable.id()          : " + idRef.id());
        System.out.println("  BaseEntity.equals()        : " + entityRef.equals(new Course("CS-101", "Копия")));
        System.out.println("  CoursePolicy.isAllowed()   : " + agePolicy.isAllowed(student, course));
        System.out.println("  Formatter<Course>.format() : " + formatter.format(course));
        System.out.println("  ClockProvider.now()        : " + fixedClock.now());

        System.out.println("\n=== 2. Архитектурные решения, мотивировка и цена изменения ===");
        printDecision(
                "1. Identifiable",
                "interface (common.Identifiable)",
                "Способность иметь ID присуща несвязанным сущностям, DTO и документам; состояние не требуется.",
                "Низкая при добавлении default-методов; высокая, если потребуется хранить мутабельное поле внутри типа."
        );
        printDecision(
                "2. CoursePolicy",
                "interface",
                "Чистая стратегия (поведение без общего состояния), может реализовываться лямбдами и внешними модулями.",
                "Добавление нового абстрактного метода сломает все реализации, если не предоставить default-реализацию."
        );
        printDecision(
                "3. BaseEntity",
                "abstract class (common.BaseEntity)",
                "Хранит общее состояние (id, createdAt, updatedAt) и единую final-реализацию equals/hashCode.",
                "Занимает единственный слот extends у подклассов (Student, Course не могут унаследовать другой класс)."
        );
        printDecision(
                "4. Formatter<T>",
                "interface с generic-параметром",
                "Чистая функция преобразования объекта T в строку, не требует базового состояния.",
                "При появлении общих настроек локали/буфера понадобится либо default-метод, либо абстрактный базовый класс."
        );
        printDecision(
                "5. ClockProvider",
                "interface (common.ClockProvider)",
                "Позволяет подменять системное время детерминированной заглушкой в модульных тестах.",
                "Минимальная (1 метод now()); при отсутствии тестов интерфейс мог бы быть избыточной обёрткой над java.time.Clock."
        );
    }

    private static void printDecision(String concept, String choice, String motivation, String cost) {
        System.out.println(concept + " -> " + choice);
        System.out.println("  Мотивировка    : " + motivation);
        System.out.println("  Цена изменения : " + cost);
    }
}

package tasks;

import common.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Задание 8. Нарушение LSP.
 * Найдите проблему в ReadOnlyCourseRepository extends CourseRepository, который выбрасывает
 * исключение из save. Перестройте интерфейсы так, чтобы read-only реализация не обещала
 * невозможную операцию.
 */
public class Task08LspViolation {

    // === 1. До рефакторинга (нарушение принципа подстановки Лисков — LSP) ===
    static class LegacyCourseRepository {
        private final List<Course> storage = new ArrayList<>();

        public void save(Course course) {
            storage.add(course);
        }

        public List<Course> findAll() {
            return List.copyOf(storage);
        }
    }

    static class LegacyReadOnlyCourseRepository extends LegacyCourseRepository {
        @Override
        public void save(Course course) {
            throw new UnsupportedOperationException("ReadOnly repository cannot save courses!");
        }
    }

    // === 2. После рефакторинга (разделение интерфейсов чтения и записи) ===
    interface ReadableCourseRepository {
        Optional<Course> findByCode(String code);
        List<Course> findAll();
    }

    interface WritableCourseRepository {
        void save(Course course);
    }

    interface CourseRepository extends ReadableCourseRepository, WritableCourseRepository {
    }

    static class ReadOnlyCourseRepository implements ReadableCourseRepository {
        private final List<Course> snapshot;

        ReadOnlyCourseRepository(List<Course> courses) {
            this.snapshot = List.copyOf(courses);
        }

        @Override
        public Optional<Course> findByCode(String code) {
            return snapshot.stream().filter(c -> c.getCode().equals(code)).findFirst();
        }

        @Override
        public List<Course> findAll() {
            return snapshot;
        }
    }

    static class InMemoryCourseRepository implements CourseRepository {
        private final List<Course> courses = new ArrayList<>();

        @Override
        public void save(Course course) {
            courses.add(course);
        }

        @Override
        public Optional<Course> findByCode(String code) {
            return courses.stream().filter(c -> c.getCode().equals(code)).findFirst();
        }

        @Override
        public List<Course> findAll() {
            return List.copyOf(courses);
        }
    }

    public static void main(String[] args) {
        Course java = new Course("JAVA-101", "Базовый Java");

        System.out.println("=== 1. Проблема в ReadOnlyCourseRepository extends CourseRepository ===");
        LegacyCourseRepository brokenPolymorphicRef = new LegacyReadOnlyCourseRepository();
        try {
            brokenPolymorphicRef.save(java);
        } catch (UnsupportedOperationException e) {
            System.out.println("  Сбой в runtime при подстановке подкласса: " + e.getMessage());
            System.out.println("  Нарушение LSP: базовый тип обещает рабочую операцию save(), а подтип ломает контракт.");
        }

        System.out.println("\n=== 2. Перестроенная иерархия интерфейсов ===");
        InMemoryCourseRepository mutableRepo = new InMemoryCourseRepository();
        mutableRepo.save(java);

        ReadableCourseRepository readOnlyRepo = new ReadOnlyCourseRepository(mutableRepo.findAll());
        System.out.println("  Чтение из ReadOnlyCourseRepository (implements ReadableCourseRepository): "
                + readOnlyRepo.findAll());
        System.out.println("  Метода save() в ReadableCourseRepository нет — невозможная операция отсечена на этапе компиляции.");
    }
}

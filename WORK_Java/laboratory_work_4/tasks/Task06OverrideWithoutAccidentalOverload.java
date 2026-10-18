package tasks;

import common.Course;

/**
 * Задание 6. Переопределение без случайной перегрузки.
 * Исправьте набор методов с ошибками в параметрах, доступе и возвращаемых типах.
 * Везде, где ожидается переопределение, используйте @Override.
 * Продемонстрируйте случай, когда код компилировался без аннотации, но вызывался не тот метод.
 */
public class Task06OverrideWithoutAccidentalOverload {

    static class BaseValidator {
        public boolean validate(Object target) {
            return target != null;
        }

        protected CharSequence summary(Course course) {
            return "Base: " + course.getCode();
        }
    }

    /**
     * Проблемный подкласс БЕЗ @Override:
     * validate(Course) не переопределяет validate(Object), а случайно перегружает его!
     */
    static class BuggyCourseValidator extends BaseValidator {
        // ОШИБКА: параметр Course вместо Object -> случайная перегрузка (overload), а не override.
        // Код компилируется без @Override, но при вызове через ссылку BaseValidator вызовется базовый метод!
        public boolean validate(Course course) {
            return course != null && course.getCredits() > 0;
        }
    }

    /**
     * Исправленный подкласс С @Override:
     * 1) Параметр совпадает с базовым (Object target).
     * 2) Модификатор доступа не сужен (protected -> public допустимо, наоборот — ошибка компиляции).
     * 3) Возвращаемый тип ковариантен (String является подтипом CharSequence).
     */
    static class FixedCourseValidator extends BaseValidator {
        @Override
        public boolean validate(Object target) {
            if (!(target instanceof Course course)) {
                return false;
            }
            return course.getCredits() > 0;
        }

        @Override
        public String summary(Course course) {
            return "Fixed: " + course.getCode() + " (" + course.getCredits() + " cr)";
        }
    }

    public static void main(String[] args) {
        Course zeroCreditCourse = new Course("ZERO-01", "Тестовый курс с 0 кредитов", 0);

        BaseValidator buggyRef = new BuggyCourseValidator();
        BaseValidator fixedRef = new FixedCourseValidator();

        System.out.println("=== 1. Случай, когда код скомпилировался без @Override, но вызвался НЕ ТОТ метод ===");
        System.out.println("  Курс: " + zeroCreditCourse);
        System.out.println("  Вызов buggyRef.validate(zeroCreditCourse) через ссылку BaseValidator:");
        System.out.println("  Результат: " + buggyRef.validate(zeroCreditCourse)
                + " (ОШИБКА: ожидалось false, но вызвался BaseValidator.validate(Object)!)");

        System.out.println("\n=== 2. Исправленный класс с @Override, корректным доступом и ковариантным возвратом ===");
        System.out.println("  Вызов fixedRef.validate(zeroCreditCourse) через ссылку BaseValidator:");
        System.out.println("  Результат: " + fixedRef.validate(zeroCreditCourse)
                + " (ВЕРНО: вызвался переопределённый FixedCourseValidator.validate(Object))");
        System.out.println("  Вызов fixedRef.summary(zeroCreditCourse): " + fixedRef.summary(zeroCreditCourse));
    }
}

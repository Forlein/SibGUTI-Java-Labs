package tasks;

import common.CompactCourseFormatter;
import common.Course;
import common.CourseFormatter;
import common.DetailedCourseFormatter;

/**
 * Задание 1. Один контракт, две реализации.
 * Объявите CourseFormatter с методом format(Course).
 * Реализуйте CompactCourseFormatter и DetailedCourseFormatter.
 * Вызовите обе реализации через переменную интерфейсного типа.
 * Объясните, что известно компилятору и что выбирается во время выполнения.
 */
public class Task01OneContractTwoImplementations {

    public static void main(String[] args) {
        Course javaCourse = new Course("SIBGUTI-101", "Основы разработки на Java", 6, true);
        Course netCourse = new Course("SIBGUTI-202", "Сетевые технологии", 4, false);

        // Обе переменные имеют одинаковый статический (compile-time) тип CourseFormatter
        CourseFormatter compact = new CompactCourseFormatter(28);
        CourseFormatter detailed = new DetailedCourseFormatter();

        System.out.println("=== 1. Вызовы через переменные интерфейсного типа CourseFormatter ===");
        System.out.println("compact.format(javaCourse):");
        System.out.println("  " + compact.format(javaCourse));
        System.out.println("detailed.format(netCourse):");
        System.out.println(detailed.format(netCourse));

        System.out.println("\n=== 2. Что известно компилятору vs что выбирается в runtime ===");
        System.out.println("Компилятору (compile-time) известно:");
        System.out.println("  - Тип обеих ссылок: " + CourseFormatter.class.getSimpleName());
        System.out.println("  - Наличие в контракте метода String format(Course) и корректность типов аргументов.");
        System.out.println("Во время выполнения (runtime) JVM через dynamic dispatch определяет:");
        System.out.println("  - Фактический класс объекта compact : " + compact.getClass().getSimpleName());
        System.out.println("  - Фактический класс объекта detailed: " + detailed.getClass().getSimpleName());
        System.out.println("  - Какую именно таблицу виртуальных методов (vtable/itable) использовать для вызова format().");
    }
}

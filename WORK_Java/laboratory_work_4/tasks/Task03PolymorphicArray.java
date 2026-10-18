package tasks;

import common.CompactCourseFormatter;
import common.Course;
import common.CourseFormatter;
import common.DetailedCourseFormatter;

/**
 * Задание 3. Полиморфный массив.
 * Сохраните несколько CourseFormatter в массиве и примените каждый к одному курсу
 * без if по типу. Добавьте третью реализацию, не меняя обход.
 * Критерий: клиент зависит только от интерфейса.
 */
public class Task03PolymorphicArray {

    /**
     * Третья реализация CourseFormatter (Markdown), добавленная без изменения клиентского обхода.
     */
    static class MarkdownCourseFormatter implements CourseFormatter {
        @Override
        public String name() {
            return "markdown";
        }

        @Override
        public String format(Course course) {
            return "## " + course.getTitle() + " (`" + course.getCode() + "`, " + course.getCredits() + " cr)";
        }
    }

    /**
     * Клиентский метод обхода: зависит ТОЛЬКО от интерфейса CourseFormatter и модели Course.
     * Не содержит if, switch или instanceof по конкретным типам форматтеров.
     */
    public static void applyAllFormatters(Course course, CourseFormatter[] formatters) {
        for (CourseFormatter formatter : formatters) {
            System.out.println("[" + formatter.name() + "] -> " + formatter.format(course).replace("\n", " | "));
        }
    }

    public static void main(String[] args) {
        Course course = new Course("JAVA-304", "Объектно-ориентированное программирование", 5, true);

        System.out.println("=== 1. Исходный массив из двух реализаций ===");
        CourseFormatter[] twoFormatters = {
                new CompactCourseFormatter(25),
                new DetailedCourseFormatter()
        };
        applyAllFormatters(course, twoFormatters);

        System.out.println("\n=== 2. Добавлена третья реализация (MarkdownCourseFormatter) — метод обхода не изменился ===");
        CourseFormatter[] threeFormatters = {
                new CompactCourseFormatter(25),
                new DetailedCourseFormatter(),
                new MarkdownCourseFormatter()
        };
        applyAllFormatters(course, threeFormatters);
    }
}

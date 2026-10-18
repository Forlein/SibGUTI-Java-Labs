package common;

/**
 * Подробная реализация CourseFormatter (Задания 1, 3).
 */
public class DetailedCourseFormatter implements CourseFormatter {

    @Override
    public String name() {
        return "detailed";
    }

    @Override
    public String format(Course course) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(course.getCode()).append(" ===\n");
        sb.append("Название : ").append(course.getTitle()).append('\n');
        sb.append("Кредиты  : ").append(course.getCredits()).append('\n');
        sb.append("Обязат.  : ").append(course.isMandatory() ? "да" : "нет");
        return sb.toString();
    }
}

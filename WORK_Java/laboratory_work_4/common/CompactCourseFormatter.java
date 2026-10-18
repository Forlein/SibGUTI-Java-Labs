package common;

/**
 * Компактная реализация CourseFormatter (Задания 1, 3).
 */
public class CompactCourseFormatter implements CourseFormatter {
    private final int maxWidth;

    public CompactCourseFormatter(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public String name() {
        return "compact";
    }

    @Override
    public String format(Course course) {
        String base = course.getCode() + " — " + course.getTitle();
        if (base.length() <= maxWidth) {
            return base;
        }
        return base.substring(0, maxWidth - 3) + "...";
    }
}

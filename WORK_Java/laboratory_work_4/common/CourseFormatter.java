package common;

/**
 * Общий контракт форматирования курса для заданий 1 и 3.
 */
public interface CourseFormatter {
    String format(Course course);

    default String name() {
        return getClass().getSimpleName();
    }
}

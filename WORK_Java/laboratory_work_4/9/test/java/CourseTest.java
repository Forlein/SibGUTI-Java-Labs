import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    // ниже минимума: -1 → ошибка
    @Test
    void completeHours_belowMinimum_throws() {
        Course c = new Course(10);
        assertThrows(IllegalArgumentException.class, () -> c.completeHours(-1));
    }

    // ровно минимум: 0 → ок
    @Test
    void completeHours_atMinimum_ok() {
        Course c = new Course(10);
        c.completeHours(0);
        assertEquals(0, c.getCompletedHours());
    }

    // ровно максимум: 10 → ок
    @Test
    void completeHours_atMaximum_ok() {
        Course c = new Course(10);
        c.completeHours(10);
        assertEquals(10, c.getCompletedHours());
    }

    // выше максимума: 11 → ошибка
    @Test
    void completeHours_aboveMaximum_throws() {
        Course c = new Course(10);
        assertThrows(IllegalStateException.class, () -> c.completeHours(11));
    }

    // «обычный» пример: 3 → ок (но НЕ проверяет границы)
    @Test
    void completeHours_normalValue_ok() {
        Course c = new Course(10);
        c.completeHours(3);
        assertEquals(3, c.getCompletedHours());
    }
}

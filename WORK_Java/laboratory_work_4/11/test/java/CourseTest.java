import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void completeHours_overMax_throwsAndStateUnchanged() {
        // Arrange
        Course c = new Course(10);
        c.completeHours(3);                       // уже 3 часа
        int before = c.getCompletedHours();       // запомнили состояние

        // Act + Assert (тип + сообщение)
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> c.completeHours(11)         // ← ОДНО действие в lambda
        );
        assertEquals("too many hours", e.getMessage());

        // Assert: объект не изменился
        assertEquals(before, c.getCompletedHours());
    }

    @Test
    void completeHours_negative_throwsAndStateUnchanged() {
        Course c = new Course(10);
        c.completeHours(4);
        int before = c.getCompletedHours();

        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> c.completeHours(-1)          // ← ОДНО действие
        );
        assertEquals("hours < 0", e.getMessage());
        assertEquals(before, c.getCompletedHours());
    }
}

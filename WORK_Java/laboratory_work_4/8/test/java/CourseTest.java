import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseTest {

    @Test
    void completeHours_addsHoursToCompleted() {
        // Arrange
        Course course = new Course(10);

        // Act
        course.completeHours(3);

        // Assert
        assertEquals(3, course.getCompletedHours(),
                "Завершённые часы должны увеличиться на 3");
    }
}
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentTest {

    @Test
    void addHours_increasesHours() {
        Enrollment e = new Enrollment("s1", "JAVA-101");   // ← свой объект
        e.addHours(3);
        assertEquals(3, e.getHours());
    }

    @Test
    void addHours_addsToExisting() {
        Enrollment e = new Enrollment("s1", "JAVA-101");   // ← свой объект
        e.addHours(3);
        e.addHours(2);
        assertEquals(5, e.getHours());
    }

    @Test
    void toString_containsFields() {
        Enrollment e = new Enrollment("s1", "JAVA-101");
        e.addHours(5);
        String s = e.toString();
        assertTrue(s.contains("s1"));                 // ← проверяем поля, не всю строку
        assertTrue(s.contains("JAVA-101"));
        assertTrue(s.contains("hours=5"));
    }
}

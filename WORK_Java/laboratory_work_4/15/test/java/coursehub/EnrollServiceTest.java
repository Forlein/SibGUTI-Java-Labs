package coursehub;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class EnrollServiceTest {

    @Test void ok() throws Exception {
        new EnrollService().enroll(new Student("s1", true), new Course("C", true, 1));
    }
    @Test void closed() {
        var e = assertThrows(AppEx.class, () -> new EnrollService().enroll(new Student("s1", true), new Course("C", false, 1)));
        assertEquals(Code.COURSE_CLOSED, e.code);
    }
    @Test void noSeats() {
        var e = assertThrows(AppEx.class, () -> new EnrollService().enroll(new Student("s1", true), new Course("C", true, 0)));
        assertEquals(Code.NO_SEATS, e.code);
    }
    @Test void prereq() {
        var e = assertThrows(AppEx.class, () -> new EnrollService().enroll(new Student("s1", false), new Course("C", true, 1)));
        assertEquals(Code.PREREQ, e.code);
    }
    @Test void repeat() throws Exception {
        var s = new EnrollService(); var st = new Student("s1", true); var c = new Course("C", true, 2);
        s.enroll(st, c);
        var e = assertThrows(AppEx.class, () -> s.enroll(st, c));
        assertEquals(Code.REPEAT, e.code);
    }
    @Test void nullStudent_programmingError() {
        assertThrows(NullPointerException.class,
            () -> new EnrollService().enroll(null, new Course("C", true, 1)));
    }
    @ParameterizedTest(name = "open={0} seats={1} prereq={2} -> {3}")
    @CsvSource({
        "false, 1, true , COURSE_CLOSED",
        "true , 0, true , NO_SEATS",
        "true , 1, false, PREREQ"
    })
    void rejectsWithCode(boolean open, int seats, boolean prereq, Code expected) {
        var e = assertThrows(AppEx.class,
            () -> new EnrollService().enroll(new Student("s1", prereq), new Course("C", open, seats)));
        assertEquals(expected, e.code);
    }
}

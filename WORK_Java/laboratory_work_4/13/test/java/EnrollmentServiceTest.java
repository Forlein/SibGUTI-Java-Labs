import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentServiceTest {

    EnrollmentService s = new EnrollmentService();

    @Test void courseClosed_rejected() {
        assertEquals(EnrollmentService.Result.REJECTED, s.enroll(false, true, true, false));
    }

    @Test void noSeats_rejected() {
        assertEquals(EnrollmentService.Result.REJECTED, s.enroll(true, false, true, false));
    }

    @Test void prereqNotDone_rejected() {
        assertEquals(EnrollmentService.Result.REJECTED, s.enroll(true, true, false, false));
    }

    @Test void repeat_rejected() {
        assertEquals(EnrollmentService.Result.REJECTED, s.enroll(true, true, true, true));
    }

    @Test void allOk_enrolled() {
        assertEquals(EnrollmentService.Result.ENROLLED, s.enroll(true, true, true, false));
    }

    // дубль правила 1: курс закрыт, остальное разное — результат тот же
    @Test void courseClosedWithOtherConditions_rejected() {
        assertEquals(EnrollmentService.Result.REJECTED, s.enroll(false, false, false, true));
    }
}

public class EnrollmentService {

    public enum Result { ENROLLED, REJECTED }

    public Result enroll(boolean courseOpen, boolean hasSeats, boolean prereqDone, boolean isRepeat) {
        if (!courseOpen) return Result.REJECTED;
        if (!hasSeats) return Result.REJECTED;
        if (!prereqDone) return Result.REJECTED;
        if (isRepeat) return Result.REJECTED;
        return Result.ENROLLED;
    }
}

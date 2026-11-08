public class Enrollment {
    private final String studentId;
    private final String courseId;
    private int hours;

    public Enrollment(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
    }

    public void addHours(int h) {
        if (h < 0) throw new IllegalArgumentException("h < 0");
        hours += h;
    }

    public int getHours() { return hours; }
    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }

    @Override public String toString() {
        return "Enrollment{studentId='" + studentId + "', courseId='" + courseId + "', hours=" + hours + "}";
    }
}

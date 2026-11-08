package coursehub;

public class Course {
    public final String code; public boolean open; public int seats;
    public Course(String c, boolean o, int s) {
        if (c == null || c.isBlank() || s < 0) throw new IllegalArgumentException();
        code = c; open = o; seats = s;
    }
}

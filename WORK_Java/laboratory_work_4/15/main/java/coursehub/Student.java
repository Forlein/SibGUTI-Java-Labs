package coursehub;

public class Student {
    public final String id; public final boolean prereq;
    public Student(String i, boolean p) {
        if (i == null || i.isBlank()) throw new IllegalArgumentException();
        id = i; prereq = p;
    }
}

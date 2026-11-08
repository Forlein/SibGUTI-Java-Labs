public class CourseCode {
    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("null");
        String s = raw.trim().toUpperCase();
        if (!s.matches("[A-Z]{2,4}-\\d{3}")) throw new IllegalArgumentException("bad format: " + raw);
        return s;
    }
}

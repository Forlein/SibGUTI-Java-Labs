package coursehub;

public class PublicApi {
    private final EnrollService es = new EnrollService();
    private final ImportService is = new ImportService();

    public String enroll(Student s, Course c) {
        try { es.enroll(s, c); return "OK"; }
        catch (AppEx e) { return "ERR:" + e.code; }
    }
    public String importAll(Reader r) {
        try { return "OK:" + is.importAll(r); }
        catch (AppEx e) { return "ERR:" + e.code; }
    }
}

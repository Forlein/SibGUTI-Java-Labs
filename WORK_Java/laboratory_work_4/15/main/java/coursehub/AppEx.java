package coursehub;

public class AppEx extends Exception {
    public final Code code;
    public AppEx(Code c, Throwable cause) { super(c.name(), cause); this.code = c; }
}

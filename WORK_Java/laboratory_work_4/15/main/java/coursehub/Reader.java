package coursehub;

public interface Reader extends AutoCloseable {
    String next() throws Exception;
}

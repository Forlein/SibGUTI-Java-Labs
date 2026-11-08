public abstract class RecordReader implements AutoCloseable {
    public abstract String readRecord() throws Exception;   // null = конец
    @Override public abstract void close() throws Exception;
}

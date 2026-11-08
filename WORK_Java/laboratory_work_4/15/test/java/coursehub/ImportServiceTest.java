package coursehub;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ImportServiceTest {

    static Reader reader(String... lines) {
        return new Reader() {
            int i = 0; final String[] d = lines;
            public String next() { return i < d.length ? d[i++] : null; }
            public void close() {}
        };
    }

    @Test void ok() throws Exception {
        assertEquals(List.of(1, 2, 3), new ImportService().importAll(reader("1", "2", "3")));
    }
    @Test void malformed_causeSaved() {
        var e = assertThrows(AppEx.class, () -> new ImportService().importAll(reader("1", "abc")));
        assertEquals(Code.BAD_RECORD, e.code);
        assertInstanceOf(NumberFormatException.class, e.getCause());
    }
    @Test void closeFailure_suppressed() {
        Reader bad = new Reader() {
            int i = 0; final String[] d = {"1", "abc"};
            public String next() { return i < d.length ? d[i++] : null; }
            public void close() throws Exception { throw new Exception("close failed"); }
        };
        var e = assertThrows(AppEx.class, () -> new ImportService().importAll(bad));
        assertEquals(Code.BAD_RECORD, e.code);
        assertEquals(1, e.getSuppressed().length);
        assertEquals("close failed", e.getSuppressed()[0].getMessage());
    }
    @Test void ioError() {
        Reader bad = new Reader() {
            public String next() throws Exception { throw new java.io.IOException("x"); }
            public void close() {}
        };
        var e = assertThrows(AppEx.class, () -> new ImportService().importAll(bad));
        assertEquals(Code.IO, e.code);
        assertInstanceOf(java.io.IOException.class, e.getCause());
    }
    @Test void nullReader_programmingError() {
        assertThrows(NullPointerException.class, () -> new ImportService().importAll(null));
    }
}

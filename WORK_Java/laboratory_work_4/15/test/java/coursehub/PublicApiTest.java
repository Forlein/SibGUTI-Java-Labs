package coursehub;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PublicApiTest {

    @Test void enroll_ok() {
        assertEquals("OK", new PublicApi().enroll(new Student("s1", true), new Course("C", true, 1)));
    }
    @Test void enroll_closed_code() {
        assertEquals("ERR:COURSE_CLOSED", new PublicApi().enroll(new Student("s1", true), new Course("C", false, 1)));
    }
    @Test void enroll_programmingError_escapes() {
        assertThrows(NullPointerException.class,
            () -> new PublicApi().enroll(null, new Course("C", true, 1)));
    }
    @Test void import_malformed_code() {
        Reader r = new Reader() {
            int i = 0; final String[] d = {"abc"};
            public String next() { return i < d.length ? d[i++] : null; }
            public void close() {}
        };
        assertEquals("ERR:BAD_RECORD", new PublicApi().importAll(r));
    }
    @Test void import_io_code() {
        Reader r = new Reader() {
            public String next() throws Exception { throw new java.io.IOException("x"); }
            public void close() {}
        };
        assertEquals("ERR:IO", new PublicApi().importAll(r));
    }
    @Test void import_ok() {
        Reader r = new Reader() {
            int i = 0; final String[] d = {"1", "2"};
            public String next() { return i < d.length ? d[i++] : null; }
            public void close() {}
        };
        assertEquals("OK:[1, 2]", new PublicApi().importAll(r));
    }
}

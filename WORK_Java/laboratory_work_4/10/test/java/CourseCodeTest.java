import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class CourseCodeTest {

    @ParameterizedTest(name = "normalize(\"{0}\") -> \"{1}\"")
    @CsvSource({
        " java-101 , JAVA-101",
        "JAVA-101  , JAVA-101",
        "  java-101, JAVA-101",
        "CS-999    , CS-999"
    })
    void normalize_valid(String input, String expected) {
        assertEquals(expected, CourseCode.normalize(input));
    }

    @ParameterizedTest(name = "normalize(\"{0}\") -> exception")
    @CsvSource({
        "''",
        "'   '",
        "JAVA101",
        "JAVA-10",
        "JAVA-1000",
        "1JAVA-101",
        "JAVA_101"
    })
    void normalize_invalid(String input) {
        assertThrows(IllegalArgumentException.class, () -> CourseCode.normalize(input));
    }
}

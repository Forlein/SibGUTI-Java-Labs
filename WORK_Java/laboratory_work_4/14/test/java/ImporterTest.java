import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ImporterTest {

    @Test
    void import_failsOnMalformedAndClose_suppressedCaptured() {
        // Arrange: 2 хорошие записи, потом битая; close тоже падает
        RecordReader reader = new RecordReader() {
            int i = 0;
            final String[] data = {"1", "2", "abc"};
            @Override public String readRecord() {
                return i < data.length ? data[i++] : null;
            }
            @Override public void close() throws Exception {
                throw new Exception("close failed");
            }
        };
        Importer importer = new Importer();

        // Act
        Exception main = assertThrows(Exception.class, () -> importer.importAll(reader));

        // Assert 1: основное — MalformedRecordException, запись №2
        assertInstanceOf(Importer.MalformedRecordException.class, main);
        Importer.MalformedRecordException mre = (Importer.MalformedRecordException) main;
        assertEquals(2, mre.recordIndex);

        // Assert 2: suppressed — ошибка закрытия, НЕ потеряна
        assertEquals(1, main.getSuppressed().length);
        assertEquals("close failed", main.getSuppressed()[0].getMessage());

        // Assert 3: обработано 2 записи (1 и 2) — до отказа
        // (проверяем косвенно: recordIndex == 2 значит 2 записи прошли)
        assertEquals(2, mre.recordIndex);

        // Assert 4: частичной публикации нет — метод бросил, список не вернулся
        // (проверяется тем, что importAll бросил, а не вернул частичный список)
        // дополнительно: повторный вызов с корректным reader вернёт полный список
        RecordReader good = new RecordReader() {
            int i = 0;
            final String[] data = {"1", "2"};
            @Override public String readRecord() { return i < data.length ? data[i++] : null; }
            @Override public void close() {}
        };
        assertDoesNotThrow(() -> {
            List<Integer> r = importer.importAll(good);
            assertEquals(List.of(1, 2), r);
        });
    }
}

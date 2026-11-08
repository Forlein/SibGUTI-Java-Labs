import java.util.ArrayList;
import java.util.List;

public class Importer {
    static class MalformedRecordException extends Exception {
        final int recordIndex;
        MalformedRecordException(int recordIndex, Throwable cause) {
            super("malformed record " + recordIndex, cause);
            this.recordIndex = recordIndex;
        }
    }

    // публикует ТОЛЬКО если всё ок; иначе — ничего
    public List<Integer> importAll(RecordReader reader) throws Exception {
        List<Integer> parsed = new ArrayList<>();
        try (reader) {
            int index = 0;
            String line;
            while ((line = reader.readRecord()) != null) {
                try {
                    parsed.add(Integer.parseInt(line));
                } catch (NumberFormatException e) {
                    throw new MalformedRecordException(index, e);
                }
                index++;
            }
        }
        return parsed;   // публикация — только после успешного закрытия
    }
}

package coursehub;

import java.util.ArrayList;
import java.util.List;

public class ImportService {
    public List<Integer> importAll(Reader r) throws AppEx {
        if (r == null) throw new NullPointerException();
        List<Integer> out = new ArrayList<>();
        try (r) {
            String line;
            while ((line = r.next()) != null) {
                try { out.add(Integer.parseInt(line)); }
                catch (NumberFormatException e) { throw new AppEx(Code.BAD_RECORD, e); }
            }
        } catch (AppEx e) { throw e; }
        catch (Exception e) { throw new AppEx(Code.IO, e); }
        return out;
    }
}

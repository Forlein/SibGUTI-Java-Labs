package coursehub;

import java.util.HashSet;
import java.util.Set;

public class EnrollService {
    private final Set<String> done = new HashSet<>();
    public void enroll(Student s, Course c) throws AppEx {
        if (!c.open)        throw new AppEx(Code.COURSE_CLOSED, null);
        if (c.seats <= 0)   throw new AppEx(Code.NO_SEATS, null);
        if (!s.prereq)      throw new AppEx(Code.PREREQ, null);
        if (!done.add(s.id + "|" + c.code)) throw new AppEx(Code.REPEAT, null);
        c.seats--;
    }
}

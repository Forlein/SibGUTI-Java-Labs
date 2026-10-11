package task_03_map_index;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class course {
    private final int course_id;
    private final String course_code;
    private String course_title;
    private int course_credits;
    private final Set<String> course_tags;

    public course(int course_id, String course_code, String course_title) {
        this(course_id, course_code, course_title, 0, new LinkedHashSet<>());
    }

    public course(int course_id, String course_code, String course_title, int course_credits, Set<String> course_tags) {
        this.course_id = course_id;
        this.course_code = course_code;
        this.course_title = course_title;
        this.course_credits = course_credits;
        this.course_tags = new LinkedHashSet<>(course_tags);
    }

    public int get_id() {
        return course_id;
    }

    public String get_code() {
        return course_code;
    }

    public String get_title() {
        return course_title;
    }

    public int get_credits() {
        return course_credits;
    }

    public Set<String> get_tags() {
        return course_tags;
    }

    public void set_title(String new_title) {
        this.course_title = new_title;
    }

    @Override
    public boolean equals(Object other_object) {
        /* Равенство только по id: код и название курса могут меняться без смены сущности. */
        if (this == other_object) return true;
        if (!(other_object instanceof course)) return false;
        course other_course = (course) other_object;
        return course_id == other_course.course_id;
    }

    @Override
    public int hashCode() {
        /* Хеш только от id, чтобы совпадать с equals и не ломаться при изменении полей. */
        return Objects.hash(course_id);
    }

    @Override
    public String toString() {
        return "course#" + course_id + "[" + course_code + " " + course_title + "]";
    }
}

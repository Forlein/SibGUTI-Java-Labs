package task_09_pecs;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class course {
    private final int course_id;
    private final String course_code;
    private String course_title;
    private final Set<String> course_tags;

    public course(int course_id, String course_code, String course_title) {
        this.course_id = course_id;
        this.course_code = course_code;
        this.course_title = course_title;
        this.course_tags = new LinkedHashSet<>();
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

    public Set<String> get_tags() {
        return course_tags;
    }

    @Override
    public boolean equals(Object other_object) {
        if (this == other_object) return true;
        if (!(other_object instanceof course)) return false;
        course other_course = (course) other_object;
        return course_id == other_course.course_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(course_id);
    }

    @Override
    public String toString() {
        return "course#" + course_id + "[" + course_code + " " + course_title + "]";
    }
}

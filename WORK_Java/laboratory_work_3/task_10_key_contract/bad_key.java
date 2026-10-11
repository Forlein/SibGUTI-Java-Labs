package task_10_key_contract;

import java.util.Objects;

public class bad_key {
    int course_id;
    String course_title;

    public bad_key(int course_id, String course_title) {
        this.course_id = course_id;
        this.course_title = course_title;
    }

    @Override
    public boolean equals(Object other_object) {
        /* Равенство по id и title; контракт ломается, потому что оба поля изменяемы. */
        if (this == other_object) return true;
        if (!(other_object instanceof bad_key)) return false;
        bad_key other_key = (bad_key) other_object;
        return course_id == other_key.course_id && Objects.equals(course_title, other_key.course_title);
    }

    @Override
    public int hashCode() {
        /* Хеш включает изменяемое title — после мутации ключ теряется в HashMap/HashSet. */
        return Objects.hash(course_id, course_title);
    }

    @Override
    public String toString() {
        return "bad_key[" + course_id + " " + course_title + "]";
    }
}

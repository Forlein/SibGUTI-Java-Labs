/*Проверьте сущности Course в HashSet и как ключи 
HashMap. Измените поле, не участвующее в равенстве, 
затем намеренно включите его в hash и воспроизведите проблему.*/

import java.util.*;

public class Main {
    static final class Course {
        final String id; String title;
        Course(String id, String title) { this.id = id; this.title = title; }
        @Override public boolean equals(Object o) { return o instanceof Course c && id.equals(c.id); }
        @Override public int hashCode() { return id.hashCode(); }               // title НЕ участвует
        @Override public String toString() { return id + ":" + title; }
    }

    static final class BadCourse {
        final String id; String title;
        BadCourse(String id, String title) { this.id = id; this.title = title; }
        @Override public boolean equals(Object o) { return o instanceof BadCourse c && id.equals(c.id); }
        @Override public int hashCode() { return Objects.hash(id, title); }      // title участвует
        @Override public String toString() { return id + ":" + title; }
    }

    public static void main(String[] args) {
        Set<Course> ok = new HashSet<>();
        Course c = new Course("C1", "A");
        ok.add(c);
        c.title = "B";                                        // меняем поле вне equals/hashCode
        System.out.println("Course contains: " + ok.contains(new Course("C1", "B")));   // true

        Set<BadCourse> bad = new HashSet<>();
        BadCourse b = new BadCourse("C1", "A");
        bad.add(b);
        b.title = "B";                                        // поле теперь в hashCode
        System.out.println("BadCourse contains self: " + bad.contains(b));              // false
        System.out.println("BadCourse size: " + bad.size() + " " + bad);                // 1, но найти нельзя
    }
}
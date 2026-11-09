/*По курсам и тегам постройте Map<Tag, Set<CourseId>>; 
поддержите добавление, удаление курса и пересечение нескольких 
тегов. Пустые set не остаются в индексе. Обоснуйте реализацию 
каждой коллекции.*/

import java.util.*;

public class Main {
    static final class TagIndex {
        private final Map<String, Set<String>> byTag = new HashMap<>();      // Tag -> CourseIds
        private final Map<String, Set<String>> courseTags = new HashMap<>(); // Course -> Tags

        void add(String courseId, String... tags) {
            Set<String> ts = courseTags.computeIfAbsent(courseId, k -> new HashSet<>());
            for (String t : tags)
                if (ts.add(t))
                    byTag.computeIfAbsent(t, k -> new HashSet<>()).add(courseId);
        }

        void removeCourse(String courseId) {
            Set<String> ts = courseTags.remove(courseId);
            if (ts == null) return;
            for (String t : ts) {
                Set<String> s = byTag.get(t);
                if (s != null) {
                    s.remove(courseId);
                    if (s.isEmpty()) byTag.remove(t);        // пустые set не остаются
                }
            }
        }

        Set<String> intersection(String... tags) {
            if (tags.length == 0) return Set.of();
            Set<String> acc = new HashSet<>(byTag.getOrDefault(tags[0], Set.of()));
            for (int i = 1; i < tags.length && !acc.isEmpty(); i++)
                acc.retainAll(byTag.getOrDefault(tags[i], Set.of()));
            return acc;
        }

        void print() { System.out.println(byTag); }
    }

    public static void main(String[] args) {
        TagIndex idx = new TagIndex();
        idx.add("C1", "java", "algo");
        idx.add("C2", "java");
        idx.add("C3", "java", "algo");

        System.out.println("java & algo = " + idx.intersection("java", "algo")); // [C1, C3]
        idx.removeCourse("C1");
        idx.print();                                                             // {java=[C3], algo=[C3]}
        idx.removeCourse("C2");
        idx.removeCourse("C3");
        idx.print();                                                             // {}
    }
}
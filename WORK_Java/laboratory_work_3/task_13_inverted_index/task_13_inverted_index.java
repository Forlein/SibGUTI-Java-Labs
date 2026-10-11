package task_13_inverted_index;

import java.util.List;

public class task_13_inverted_index {

    public static void main(String[] args) {
        /* Задача 13: обратный индекс Map<Tag, Set<CourseId>> — добавление, удаление и пересечение тегов. */
        inverted_index tag_index = new inverted_index();
        tag_index.add_course(1, List.of("java", "backend"));
        tag_index.add_course(2, List.of("java", "алгоритмы"));
        tag_index.add_course(3, List.of("java", "backend", "spring"));

        System.out.println("Индекс: " + tag_index.snapshot());
        System.out.println("Курсы с java: " + tag_index.find_by_tag("java"));
        System.out.println("java + backend: " + tag_index.intersect_tags(List.of("java", "backend")));

        tag_index.remove_course(2);
        System.out.println("После удаления курса 2: " + tag_index.snapshot());
    }
}

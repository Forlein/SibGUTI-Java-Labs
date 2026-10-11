package task_13_inverted_index;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class inverted_index {
    private final Map<String, Set<Integer>> course_ids_by_tag = new LinkedHashMap<>();

    public void add_course(int course_id, List<String> tag_list) {
        /* Добавляет курс во все теги; для нового тега сам создаёт set. */
        for (String current_tag : tag_list) {
            course_ids_by_tag.computeIfAbsent(current_tag, new_tag -> new HashSet<>()).add(course_id);
        }
    }

    public void remove_course(int course_id) {
        /* Удаляет курс из всех тегов и чистит опустевшие set, чтобы индекс не разрастался. */
        var tag_iterator = course_ids_by_tag.entrySet().iterator();
        while (tag_iterator.hasNext()) {
            Map.Entry<String, Set<Integer>> tag_entry = tag_iterator.next();
            tag_entry.getValue().remove(course_id);
            if (tag_entry.getValue().isEmpty()) {
                tag_iterator.remove();
            }
        }
    }

    public Set<Integer> find_by_tag(String tag) {
        /* Возвращает копию множества курсов по тегу, не отдавая внутреннее состояние наружу. */
        return new HashSet<>(course_ids_by_tag.getOrDefault(tag, Set.of()));
    }

    public Set<Integer> intersect_tags(List<String> tag_list) {
        /* Пересечение тегов: оставляет id, встречающиеся во всех наборах; неизвестный тег даёт пусто. */
        Set<Integer> result_ids = null;
        for (String current_tag : tag_list) {
            Set<Integer> tag_course_ids = course_ids_by_tag.get(current_tag);
            if (tag_course_ids == null) {
                return new HashSet<>();
            }
            if (result_ids == null) {
                result_ids = new HashSet<>(tag_course_ids);
            } else {
                result_ids.retainAll(tag_course_ids);
            }
        }
        return result_ids == null ? new HashSet<>() : result_ids;
    }

    public Map<String, Set<Integer>> snapshot() {
        /* Копия индекса для диагностики; LinkedHashMap держит порядок тегов, HashSet — уникальные id. */
        Map<String, Set<Integer>> index_copy = new LinkedHashMap<>();
        for (Map.Entry<String, Set<Integer>> tag_entry : course_ids_by_tag.entrySet()) {
            index_copy.put(tag_entry.getKey(), new HashSet<>(tag_entry.getValue()));
        }
        return index_copy;
    }
}

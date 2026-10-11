package task_03_map_index;

import java.util.HashMap;
import java.util.Map;

public class task_03_map_index {

    public static void main(String[] args) {
        /* Задача 3: Map и индекс — CourseId -> Course с обработкой повторного ID без молчаливой перезаписи. */
        Map<Integer, course> course_by_id = new HashMap<>();
        add_course(course_by_id, new course(1, "CS101", "Введение в программирование"));
        add_course(course_by_id, new course(2, "MATH12", "Матанализ"));
        add_course(course_by_id, new course(1, "CS101X", "Дубликат"));

        System.out.println("Индекс: " + course_by_id);

        int probe_id = 3;
        course probe_course = new course(probe_id, "PHYS01", "Физика");
        System.out.println("containsKey(3) до put: " + course_by_id.containsKey(probe_id));
        System.out.println("get(3) до put: " + course_by_id.get(probe_id));

        course previous_course = course_by_id.putIfAbsent(probe_id, probe_course);
        System.out.println("putIfAbsent вернул: " + previous_course + ", теперь: " + course_by_id.get(probe_id));

        course existing_course = course_by_id.computeIfAbsent(probe_id, id -> new course(id, "PHYS02", "Другая физика"));
        System.out.println("computeIfAbsent на существующем ключе вернул: " + existing_course);
        course created_course = course_by_id.computeIfAbsent(4, id -> new course(id, "BIO01", "Биология"));
        System.out.println("computeIfAbsent на новом ключе создал: " + created_course);
    }

    static void add_course(Map<Integer, course> course_by_id, course new_course) {
        /* Обёртка над строгим добавлением: печатает конфликт ID вместо тихой перезаписи. */
        if (!course_index.add_course_strict(course_by_id, new_course)) {
            System.out.println("ID " + new_course.get_id() + " уже занят, курс " + new_course.get_code() + " не добавлен");
        }
    }
}

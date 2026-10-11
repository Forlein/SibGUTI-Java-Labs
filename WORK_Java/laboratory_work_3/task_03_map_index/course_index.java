package task_03_map_index;

import java.util.Map;

public class course_index {

    public static boolean add_course_strict(Map<Integer, course> course_by_id, course new_course) {
        /* Добавляет курс, не перезаписывая занятый ID: putIfAbsent возвращает старый объект при коллизии. */
        course existing_course = course_by_id.putIfAbsent(new_course.get_id(), new_course);
        return existing_course == null;
    }
}

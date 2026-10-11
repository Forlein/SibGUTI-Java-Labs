package task_02_set_uniqueness;

import java.util.ArrayList;
import java.util.List;

public class task_02_set_uniqueness {

    public static void main(String[] args) {
        /* Задача 2: Set и уникальность — убрать дубликаты CourseCode, сохранив порядок появления. */
        List<String> course_code_list = new ArrayList<>(List.of("CS101", "MATH12", "CS101", "PHYS01", "MATH12", "CS202"));

        System.out.println("Исходный список: " + course_code_list);
        System.out.println("Уникальные с сохранением порядка: " + code_deduplicator.deduplicate(course_code_list));
        System.out.println("Пропущенные дубликаты: " + code_deduplicator.find_skipped_duplicates(course_code_list));
        /* equals решает «кто дубликат», hashCode задаёт корзину; плохой hashCode ломает уникальность Set. */
    }
}

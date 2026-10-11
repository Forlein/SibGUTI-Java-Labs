package task_10_key_contract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class task_10_key_contract {

    public static void main(String[] args) {
        /* Задача 10: контракт ключа — course в HashSet/HashMap, изменение поля и поломка хеша. */
        Set<course> course_set = new HashSet<>();
        course stable_course = new course(1, "CS101", "Программирование");
        course_set.add(stable_course);
        System.out.println("contains до изменения title: " + course_set.contains(stable_course));

        stable_course.set_title("Другое название");
        System.out.println("title не входит в equals/hashCode, поэтому contains: " + course_set.contains(stable_course));

        Map<course, Integer> credits_by_course = new HashMap<>();
        course map_key = new course(2, "MATH12", "Матанализ");
        credits_by_course.put(map_key, 100);
        map_key.set_title("Изменённый матанализ");
        System.out.println("Значение по ключу после правки title: " + credits_by_course.get(map_key));

        bad_key mutable_key = new bad_key(3, "Физика");
        Map<bad_key, String> broken_map = new HashMap<>();
        broken_map.put(mutable_key, "Физика");
        mutable_key.course_title = "Новая физика";
        System.out.println("Правильный ключ всё ещё там? " + broken_map.containsKey(mutable_key));
        System.out.println("Размер map: " + broken_map.size() + ", содержимое: " + broken_map);
        /* hashCode зависит от title, после мутации ключ попал в чужую корзину — элемент «потерян». */
    }
}

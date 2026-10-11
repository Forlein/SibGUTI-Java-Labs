package task_07_generic_methods;

import java.util.ArrayList;
import java.util.List;

public class task_07_generic_methods {

    public static void main(String[] args) {
        /* Задача 7: generic-методы firstOrThrow, swap и indexBy с явной обработкой конфликта ключей. */
        List<String> word_list = new ArrayList<>(List.of("alpha", "beta", "gamma"));
        System.out.println("first_or_throw: " + collection_utils.first_or_throw(word_list));

        collection_utils.swap(word_list, 0, 2);
        System.out.println("после swap(0, 2): " + word_list);

        try {
            collection_utils.first_or_throw(new ArrayList<String>());
        } catch (Exception empty_list_error) {
            System.out.println("first_or_throw на пустом: " + empty_list_error.getMessage());
        }

        List<course> course_list = List.of(
                new course(1, "CS101", "Программирование"),
                new course(2, "MATH12", "Матанализ"),
                new course(3, "CS101", "Алгоритмы"));

        System.out.println("index_by по id: " + collection_utils.index_by(course_list, course::get_id));
        try {
            collection_utils.index_by(course_list, course::get_code);
        } catch (Exception key_conflict_error) {
            System.out.println("index_by по code: " + key_conflict_error.getMessage());
        }
    }
}

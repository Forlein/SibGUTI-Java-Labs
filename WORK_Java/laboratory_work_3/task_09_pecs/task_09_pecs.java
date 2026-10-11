package task_09_pecs;

import java.util.ArrayList;
import java.util.List;

public class task_09_pecs {

    public static void main(String[] args) {
        /* Задача 9: PECS — копирование из producer-списка (? extends) в consumer-список (? super). */
        List<course> source_list = new ArrayList<>(List.of(
                new course(1, "CS101", "Программирование"),
                new course(2, "MATH12", "Матанализ")));
        List<course> course_target = new ArrayList<>();
        collection_copy.copy(source_list, course_target);
        System.out.println("Скопировано в List<course>: " + course_target);

        List<Object> object_target = new ArrayList<>();
        collection_copy.copy(source_list, object_target);
        System.out.println("Скопировано в List<Object>: " + object_target);

        List<? extends course> producer_list = source_list;
        course first_course = producer_list.get(0);
        System.out.println("Из List<? extends course> читаем как course: " + first_course);
        /* producer_list.add(...) не скомпилируется: компилятор не знает точный подтип элемента. */

        List<? super course> consumer_list = new ArrayList<>();
        consumer_list.add(new course(4, "BIO01", "Биология"));
        Object only_object = consumer_list.get(0);
        System.out.println("В List<? super course> пишем course, читаем только Object: " + only_object);
    }
}

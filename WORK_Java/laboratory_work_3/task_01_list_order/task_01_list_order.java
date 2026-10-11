package task_01_list_order;

import java.util.ArrayList;
import java.util.List;

public class task_01_list_order {

    public static void main(String[] args) {
        /* Задача 1: List и порядок — регистрации, вставка, удаление, дубликаты и сравнение реализаций. */
        List<String> registration_list = new ArrayList<>();
        registration_list.add("Иванов");
        registration_list.add("Петров");
        registration_list.add("Сидоров");
        registration_list.add("Иванов");
        registration_list.add("Кузнецова");

        System.out.println("Порядок регистраций: " + registration_list);
        System.out.println("Дубликаты: " + list_analyzer.find_duplicates(registration_list));

        registration_list.add(1, "Смирнов");
        System.out.println("После вставки на позицию 1: " + registration_list);

        registration_list.remove("Иванов");
        System.out.println("После удаления первого 'Иванов': " + registration_list);

        list_analyzer.compare_read_and_iteration(200000);
    }
}

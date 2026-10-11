package task_01_list_order;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class list_analyzer {

    public static List<String> find_duplicates(List<String> name_list) {
        /* Ищет повторяющиеся имена, сохраняя порядок их первого повторного появления. */
        List<String> seen_names = new ArrayList<>();
        List<String> duplicate_names = new ArrayList<>();
        for (String current_name : name_list) {
            if (seen_names.contains(current_name)) {
                if (!duplicate_names.contains(current_name)) {
                    duplicate_names.add(current_name);
                }
            } else {
                seen_names.add(current_name);
            }
        }
        return duplicate_names;
    }

    public static void compare_read_and_iteration(int element_count) {
        /* Меряет чтение по индексу и полный обход ArrayList против LinkedList. Это ориентир, а не микробенчмарк. */
        ArrayList<Integer> array_list = new ArrayList<>();
        LinkedList<Integer> linked_list = new LinkedList<>();
        for (int index = 0; index < element_count; index++) {
            array_list.add(index);
            linked_list.add(index);
        }

        long read_start = System.nanoTime();
        long array_sum = 0;
        for (int index = 0; index < element_count; index++) {
            array_sum += array_list.get(index);
        }
        long array_read_time = System.nanoTime() - read_start;

        long linked_read_start = System.nanoTime();
        long linked_sum = 0;
        for (int index = 0; index < element_count; index++) {
            linked_sum += linked_list.get(index);
        }
        long linked_read_time = System.nanoTime() - linked_read_start;

        long array_iter_start = System.nanoTime();
        long array_iter_sum = 0;
        for (int value : array_list) {
            array_iter_sum += value;
        }
        long array_iter_time = System.nanoTime() - array_iter_start;

        long linked_iter_start = System.nanoTime();
        long linked_iter_sum = 0;
        for (int value : linked_list) {
            linked_iter_sum += value;
        }
        long linked_iter_time = System.nanoTime() - linked_iter_start;

        System.out.println("Чтение по индексу: ArrayList=" + array_read_time / 1_000_000 + " мс, LinkedList=" + linked_read_time / 1_000_000 + " мс");
        System.out.println("Обход: ArrayList=" + array_iter_time / 1_000_000 + " мс, LinkedList=" + linked_iter_time / 1_000_000 + " мс");
        System.out.println("Контрольные суммы: " + array_sum + ", " + linked_sum + ", " + array_iter_sum + ", " + linked_iter_sum);

        if (array_read_time < linked_read_time) {
            /* Замер подтверждает выбор: по индексу ArrayList быстрее, обход сопоставим — берём ArrayList. */
            System.out.println("Вывод: для доступа по индексу выбираем ArrayList, LinkedList выгоден при частых вставках/удалениях.");
        } else {
            System.out.println("Вывод: в этом прогоне LinkedList не проиграл, но обычно доступ по индексу у ArrayList O(1).");
        }
    }
}

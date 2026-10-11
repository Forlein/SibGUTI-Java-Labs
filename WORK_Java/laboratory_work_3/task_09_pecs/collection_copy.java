package task_09_pecs;

import java.util.List;

public class collection_copy {

    public static <T> void copy(List<? extends T> source_list, List<? super T> destination_list) {
        /* Копирует из producer (только читаем) в consumer (только пишем) — классический пример PECS. */
        for (T current_element : source_list) {
            destination_list.add(current_element);
        }
    }
}

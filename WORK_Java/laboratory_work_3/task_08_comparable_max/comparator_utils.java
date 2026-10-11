package task_08_comparable_max;

import java.util.List;

public class comparator_utils {

    public static <T extends Comparable<? super T>> T max(List<? extends T> element_list) {
        /* Возвращает максимум; bound <? super T> позволяет сравнивать подтипы через их супертип. */
        if (element_list.isEmpty()) {
            throw new IllegalArgumentException("Пустой список");
        }
        T best_element = null;
        for (T current_element : element_list) {
            if (best_element == null || current_element.compareTo(best_element) > 0) {
                best_element = current_element;
            }
        }
        return best_element;
    }
}

package task_05_safe_removal;

import java.util.Iterator;
import java.util.List;

public class list_cleaner {

    public static void remove_via_iterator(List<String> target_list, String value_to_remove) {
        /* Безопасное удаление через Iterator.remove(): итератор знает об изменении размера. */
        Iterator<String> iterator = target_list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(value_to_remove)) {
                iterator.remove();
            }
        }
    }

    public static void remove_via_remove_if(List<String> target_list, String value_to_remove) {
        /* removeIf делегирует ту же безопасную логику и читается короче. */
        target_list.removeIf(current_value -> current_value.equals(value_to_remove));
    }
}

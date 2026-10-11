package task_07_generic_methods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class collection_utils {

    public static <T> T first_or_throw(List<T> element_list) {
        /* Возвращает первый элемент или бросает исключение, если список пуст. */
        if (element_list.isEmpty()) {
            throw new IllegalArgumentException("Список пуст");
        }
        return element_list.get(0);
    }

    public static <T> void swap(List<T> element_list, int first_index, int second_index) {
        /* Меняет местами два элемента по индексам, проверяя границы. */
        if (first_index < 0 || second_index < 0 || first_index >= element_list.size() || second_index >= element_list.size()) {
            throw new IndexOutOfBoundsException("Некорректные индексы " + first_index + ", " + second_index);
        }
        T saved_value = element_list.get(first_index);
        element_list.set(first_index, element_list.get(second_index));
        element_list.set(second_index, saved_value);
    }

    public static <T, K> Map<K, T> index_by(List<T> element_list, Function<T, K> key_extractor) {
        /* Строит индекс ключ->элемент; при конфликте ключей бросает исключение, а не молча перезаписывает. */
        Map<K, T> element_by_key = new HashMap<>();
        for (T current_element : element_list) {
            K current_key = key_extractor.apply(current_element);
            if (element_by_key.putIfAbsent(current_key, current_element) != null) {
                throw new IllegalStateException("Конфликт ключа: " + current_key);
            }
        }
        return element_by_key;
    }
}

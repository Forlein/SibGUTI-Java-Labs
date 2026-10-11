package task_11_load_choice;

import java.util.List;

public class task_11_load_choice {

    public static void main(String[] args) {
        /* Задача 11: выбор интерфейса и реализации под шесть сценариев нагрузки с оценкой сложности. */
        List<load_scenario> scenario_list = List.of(
                new load_scenario("Каталог по ID", "HashMap", "get/put O(1), память O(n) по ключам"),
                new load_scenario("Отсортированный leaderboard", "TreeMap или List+sort", "обход O(n), вставка O(log n), порядок по ключу"),
                new load_scenario("Очередь заявок", "ArrayDeque", "add/poll O(1), строгий FIFO-порядок"),
                new load_scenario("Уникальные теги с порядком", "LinkedHashSet", "add/contains O(1), порядок вставки"),
                new load_scenario("Диапазонный поиск по баллам", "TreeMap (NavigableMap)", "subMap O(log n), отсортированный порядок"),
                new load_scenario("Частый доступ по индексу", "ArrayList", "get O(1), вставка в середину O(n)"));

        int scenario_number = 1;
        for (load_scenario current_scenario : scenario_list) {
            System.out.println(scenario_number + ") " + current_scenario);
            scenario_number++;
        }
    }
}

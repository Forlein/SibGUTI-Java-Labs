/*Для шести сценариев — каталог по ID, отсортированный 
leaderboard, очередь, уникальные теги с порядком, диапазонный 
поиск, частый доступ по индексу — выберите интерфейс и реализацию. 
Укажите сложность главных операций и цену памяти/порядка.*/

import java.util.*;

public class Main {
    record Course(String id) {}
    record Score(int points) {}

    public static void main(String[] args) {
        // 1) каталог по ID — быстрый доступ по ключу, порядок не важен
        Map<String, Course> catalog = new HashMap<>();
        catalog.put("C1", new Course("C1"));
        catalog.get("C1");

        // 2) leaderboard — отсортирован по очкам, нужен максимум/топ
        NavigableSet<Score> leaderboard = new TreeSet<>(Comparator.comparingInt(Score::points).reversed());
        leaderboard.add(new Score(10));
        leaderboard.add(new Score(30));
        leaderboard.add(new Score(20));
        leaderboard.first();

        // 3) очередь — FIFO
        Queue<String> queue = new ArrayDeque<>();
        queue.offer("a");
        queue.poll();

        // 4) уникальные теги с порядком появления
        Set<String> tags = new LinkedHashSet<>();
        tags.add("java");
        tags.add("algo");

        // 5) диапазонный поиск по числовому ключу
        NavigableMap<Integer, String> byPrice = new TreeMap<>();
        byPrice.put(100, "a");
        byPrice.put(250, "b");
        byPrice.subMap(100, true, 200, true);

        // 6) частый доступ по индексу
        List<Course> list = new ArrayList<>();
        list.add(new Course("C1"));
        list.get(0);
    }
}
/*
| Сценарий                  | Выбор                       | Почему                                    | Ключевые операции                              | Цена                              |
|---------------------------|-----------------------------|-------------------------------------------|------------------------------------------------|-----------------------------------|
| Каталог по ID             | Map / HashMap               | быстрый доступ по ключу, порядок не нужен | get/put — O(1)                                 | O(n) памяти, без порядка          |
| Leaderboard               | NavigableSet / TreeSet      | нужен отсортированный порядок, топ/максимум | add/first — O(log n)                         | O(n), красно-чёрное дерево        |
| Очередь                   | Queue / ArrayDeque          | FIFO, быстро с обоих концов               | offer/poll — O(1) амортиз.                     | кольцевой массив, O(n)            |
| Уникальные теги с порядком| Set / LinkedHashSet         | уникальность + порядок вставки            | add/contains — O(1)                            | +двусвязный список для порядка    |
| Диапазонный поиск         | NavigableMap / TreeMap      | нужны subMap, floor, ceiling              | put/get — O(log n), subMap — O(log n + k)      | O(n), красно-чёрное дерево        |
| Частый доступ по индексу  | List / ArrayList            | get(i) за O(1)                            | add — O(1) аморт., get — O(1)                  | непрерывный массив, O(n)          |
*/

package task_14_lru_cache;

public class task_14_lru_cache {

    public static void main(String[] args) {
        /* Задача 14: generic LRU-кэш фиксированной ёмкости на LinkedHashMap; показываем порядок вытеснения. */
        lru_cache<String, Integer> cache = new lru_cache<>(3);
        cache.put("A", 1);
        cache.put("B", 2);
        cache.put("C", 3);
        System.out.println("Кэш: " + cache);

        System.out.println("get(A)=" + cache.get("A"));
        cache.put("D", 4);
        System.out.println("После вставки D (A использован, вытесняется B): " + cache);
        System.out.println("Содержит B? " + cache.contains_key("B"));

        cache.put("E", null);
        System.out.println("null-значение разрешено и хранится: " + cache);

        cache.put("D", 40);
        System.out.println("Повторный put(D, 40) меняет значение и двигает D в конец, ключей по-прежнему 3: " + cache);
    }
}

package task_14_lru_cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class lru_cache<K, V> {
    private final int max_capacity;
    private final LinkedHashMap<K, V> cache_map;

    public lru_cache(int max_capacity) {
        /* Фиксированная ёмкость; accessOrder=true двигает get к концу, removeEldestEntry вытесняет первый. */
        if (max_capacity <= 0) {
            throw new IllegalArgumentException("max_capacity > 0");
        }
        this.max_capacity = max_capacity;
        this.cache_map = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest_entry) {
                /* true при превышении ёмкости, и тогда LinkedHashMap удаляет старейший элемент. */
                return size() > lru_cache.this.max_capacity;
            }
        };
    }

    public V get(K cache_key) {
        /* Читает значение и обновляет недавность ключа. */
        return cache_map.get(cache_key);
    }

    public void put(K cache_key, V cache_value) {
        /* Кладёт значение; null-значение и null-ключ разрешены явно. */
        cache_map.put(cache_key, cache_value);
    }

    public boolean contains_key(K cache_key) {
        /* Проверяет наличие ключа без изменения порядка недавности. */
        return cache_map.containsKey(cache_key);
    }

    @Override
    public String toString() {
        return cache_map.toString();
    }
}

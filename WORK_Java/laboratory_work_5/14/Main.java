/*Реализуйте generic LruCache<K,V> фиксированной ёмкости 
на стандартных коллекциях. get обновляет недавность, превышение 
удаляет старейший элемент, null-политика явна. Докажите сценариями 
порядок вытеснения и стабильность ключей.*/

import java.util.*;

public class Main {
    static final class LruCache<K, V> {
        private final LinkedHashMap<K, V> map;

        LruCache(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity>0");
            this.map = new LinkedHashMap<>(16, 0.75f, true) {   // accessOrder = true
                @Override protected boolean removeEldestEntry(Map.Entry<K, V> e) {
                    return size() > capacity;
                }
            };
        }

        V get(K k) { Objects.requireNonNull(k); return map.get(k); }
        void put(K k, V v) { Objects.requireNonNull(k); Objects.requireNonNull(v); map.put(k, v); }
        List<K> keys() { return new ArrayList<>(map.keySet()); }
    }

    public static void main(String[] args) {
        LruCache<String, Integer> c = new LruCache<>(2);

        c.put("a", 1);
        c.put("b", 2);
        System.out.println(c.keys());   // [a, b]

        c.get("a");                     // "a" стал свежим
        c.put("c", 3);                  // вытесняется "b"
        System.out.println(c.keys());   // [a, c]

        c.put("a", 10);                 // обновление значения, ключ тот же
        System.out.println(c.get("a") + " " + c.keys());   // 10 [a, c]

        c.put("d", 4);                  // вытесняется "c"
        System.out.println(c.keys());   // [a, d]
    }
}
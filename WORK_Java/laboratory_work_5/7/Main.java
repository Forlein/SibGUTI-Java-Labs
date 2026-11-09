/*Напишите firstOrThrow(List<T>), swap(List<T>, int, int) 
и indexBy(List<T>, Function<T,K>). Последний метод должен 
явно обрабатывать конфликт ключей.*/

import java.util.*;
import java.util.function.Function;

public class Main {
    static <T> T firstOrThrow(List<T> list) {
        if (list == null || list.isEmpty()) throw new NoSuchElementException("empty");
        return list.get(0);
    }

    static <T> void swap(List<T> list, int i, int j) {
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    static <T, K> Map<K, T> indexBy(List<T> list, Function<T, K> keyFn) {
        Map<K, T> map = new HashMap<>();
        for (T t : list) {
            K k = keyFn.apply(t);
            if (k == null) throw new IllegalArgumentException("null key");
            if (map.putIfAbsent(k, t) != null)
                throw new IllegalStateException("duplicate key: " + k);
        }
        return map;
    }

    public static void main(String[] args) {
        System.out.println(firstOrThrow(List.of("a", "b")));
        try { firstOrThrow(List.of()); } catch (NoSuchElementException e) { System.out.println("empty"); }

        List<Integer> l = new ArrayList<>(List.of(1, 2, 3));
        swap(l, 0, 2);
        System.out.println(l);

        System.out.println(indexBy(List.of("aa", "bb", "cc"), s -> s.charAt(0)));
        try { indexBy(List.of("aa", "ab"), s -> s.charAt(0)); }
        catch (IllegalStateException e) { System.out.println(e.getMessage()); }
    }
}

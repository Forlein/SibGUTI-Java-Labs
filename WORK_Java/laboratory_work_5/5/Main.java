/*Воспроизведите ConcurrentModificationException при удалении из 
list в enhanced for. Исправьте через iterator и через removeIf. 
Объясните, почему копирование всей коллекции не всегда лучший default.*/

import java.util.*;

public class Main {
    public static void main(String[] args) {
        // 1) как падает
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        try {
            for (Integer x : a) if (x % 2 == 0) a.remove(x);
        } catch (ConcurrentModificationException e) {
            System.out.println("CME caught");
        }

        // 2) фикс через iterator
        List<Integer> b = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = b.iterator();
        while (it.hasNext()) if (it.next() % 2 == 0) it.remove();
        System.out.println("iterator: " + b);

        // 3) фикс через removeIf
        List<Integer> c = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        c.removeIf(x -> x % 2 == 0);
        System.out.println("removeIf: " + c);
    }
}
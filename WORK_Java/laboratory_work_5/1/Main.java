/*Сохраните последовательность регистраций, вставьте 
и удалите элемент, найдите дубликаты. Сравните ArrayList 
и LinkedList для чтения по индексу и обхода; выбор подтвердите 
измерением без заявления о микробенчмарке.*/

import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<String> regs = new ArrayList<>(List.of("A", "B", "C", "B", "D"));
        regs.add(2, "X");
        regs.remove("B");
        Set<String> seen = new HashSet<>(), dup = new LinkedHashSet<>();
        for (String s : regs) if (!seen.add(s)) dup.add(s);
        System.out.println("regs=" + regs + " dup=" + dup);

        int N = 200_000;
        List<Integer> al = new ArrayList<>(), ll = new LinkedList<>();
        for (int i = 0; i < N; i++) { al.add(i); ll.add(i); }

        long t = System.nanoTime();
        for (int i = 0; i < N; i++) al.get(i);
        long alMs = (System.nanoTime() - t) / 1_000_000;

        t = System.nanoTime();
        for (int i = 0; i < N; i++) ll.get(i);
        long llMs = (System.nanoTime() - t) / 1_000_000;

        System.out.println("get: ArrayList=" + alMs + "ms, LinkedList=" + llMs + "ms");
    }
}
/*Постройте индекс CourseId -> Course, обработайте 
повторный ID без молчаливой перезаписи. Сравните get, 
containsKey, putIfAbsent и computeIfAbsent.*/

import java.util.*;

public class Main {
    record Course(String id, String title) {}

    public static void main(String[] args) {
        List<Course> input = List.of(
                new Course("C1", "A"),
                new Course("C2", "B"),
                new Course("C1", "A-dup"));

        // 1) get: только чтение, null не отличает "нет ключа" от "значение null"
        Map<String, Course> m1 = new HashMap<>();
        for (Course c : input) m1.put(c.id(), c);          // тут перезапись молча
        System.out.println("get: " + m1.get("C1"));        // A-dup — старый затёрт молча

        // 2) containsKey + get: проверка и чтение раздельно, два прохода
        Map<String, Course> m2 = new HashMap<>();
        for (Course c : input) {
            if (m2.containsKey(c.id())) System.out.println("containsKey: dup " + c.id());
            else m2.put(c.id(), c);
        }

        // 3) putIfAbsent: вставка с проверкой одним вызовом, возвращает старое
        Map<String, Course> m3 = new HashMap<>();
        for (Course c : input) {
            Course prev = m3.putIfAbsent(c.id(), c);
            if (prev != null) System.out.println("putIfAbsent: dup " + c.id() + ", kept " + prev);
        }

        // 4) computeIfAbsent: значение вычисляется только если ключа нет
        Map<String, Course> m4 = new HashMap<>();
        for (Course c : input)
            m4.computeIfAbsent(c.id(), k -> c);            // дубль просто игнорируется
        System.out.println("computeIfAbsent: " + m4);
    }
}
/*Реализуйте max для элементов Comparable. Корректно 
сформулируйте bound с учётом подтипов. Проверьте на Integer, 
String и собственном value object.*/

import java.util.*;

public class Main {
    static <T extends Comparable<? super T>> T max(Collection<? extends T> c) {
        Iterator<? extends T> it = c.iterator();
        if (!it.hasNext()) throw new NoSuchElementException("empty");
        T best = it.next();
        while (it.hasNext()) {
            T x = it.next();
            if (x.compareTo(best) > 0) best = x;
        }
        return best;
    }

    record Version(int major, int minor) implements Comparable<Version> {
        public int compareTo(Version o) {
            return major != o.major
                    ? Integer.compare(major, o.major)
                    : Integer.compare(minor, o.minor);
        }
    }

    public static void main(String[] args) {
        System.out.println(max(List.of(3, 1, 2)));                    // Integer
        System.out.println(max(List.of("b", "c", "a")));              // String
        System.out.println(max(List.of(new Version(1, 2), new Version(1, 10)))); // свой
    }
}
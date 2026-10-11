package task_08_comparable_max;

import java.util.List;

public class task_08_comparable_max {

    public static void main(String[] args) {
        /* Задача 8: generic max для Comparable с корректным bound и проверкой на разных типах. */
        System.out.println("Integer max: " + comparator_utils.max(List.of(3, 7, 2, 9, 5)));
        System.out.println("String max: " + comparator_utils.max(List.of("b", "d", "a", "c")));

        List<money> money_list = List.of(new money(10), new money(50), new money(25));
        System.out.println("money max: " + comparator_utils.max(money_list));

        List<Integer> integer_list = List.of(1, 2, 3);
        System.out.println("Integer через List<Integer>: " + comparator_utils.max(integer_list));
    }
}

package task_05_safe_removal;

import java.util.ArrayList;
import java.util.List;

public class task_05_safe_removal {

    public static void main(String[] args) {
        /* Задача 5: воспроизводим ConcurrentModificationException и чиним через iterator и removeIf. */
        List<String> name_list = new ArrayList<>(List.of("A", "B", "C", "D"));

        try {
            for (String current_name : name_list) {
                if (current_name.equals("B")) {
                    name_list.remove(current_name);
                }
            }
        } catch (Exception modification_error) {
            System.out.println("Воспроизведено: " + modification_error.getClass().getSimpleName());
        }

        name_list = new ArrayList<>(List.of("A", "B", "C", "D"));
        list_cleaner.remove_via_iterator(name_list, "B");
        System.out.println("После удаления через iterator: " + name_list);

        name_list = new ArrayList<>(List.of("A", "B", "C", "D"));
        list_cleaner.remove_via_remove_if(name_list, "B");
        System.out.println("После removeIf: " + name_list);
        /* Копирование всей коллекции решает задачу, но тратит O(n) памяти и времени и может скрывать ошибку. */
    }
}

package task_04_waitlist_queue;

import java.util.ArrayDeque;
import java.util.Queue;

public class waitlist {
    private final Queue<String> student_queue = new ArrayDeque<>();

    public void add_student(String student_name) {
        /* Добавляет студента в конец очереди; offer не бросает исключение в отличие от add. */
        student_queue.offer(student_name);
    }

    public String peek_next() {
        /* Смотрит следующего без извлечения; на пустой очереди вернёт null. */
        return student_queue.peek();
    }

    public String poll_next() {
        /* Извлекает следующего по FIFO; на пустой очереди вернёт null. */
        return student_queue.poll();
    }

    public boolean is_empty() {
        return student_queue.isEmpty();
    }

    public int size() {
        return student_queue.size();
    }
}

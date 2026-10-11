package task_04_waitlist_queue;

import java.util.ArrayDeque;
import java.util.Queue;

public class task_04_waitlist_queue {

    public static void main(String[] args) {
        /* Задача 4: FIFO waitlist через Queue — добавить, посмотреть, извлечь, обработать пустую очередь. */
        waitlist course_waitlist = new waitlist();
        course_waitlist.add_student("Студент-1");
        course_waitlist.add_student("Студент-2");
        course_waitlist.add_student("Студент-3");

        System.out.println("Следующий без извлечения: " + course_waitlist.peek_next());
        System.out.println("Извлечён: " + course_waitlist.poll_next());

        while (!course_waitlist.is_empty()) {
            System.out.println("Обработан: " + course_waitlist.poll_next());
        }
        /* Пары: add бросает исключение / offer вернёт false; remove бросает / poll вернёт null; element бросает / peek вернёт null. */
        demonstrate_empty_queue(new ArrayDeque<>());
    }

    static void demonstrate_empty_queue(Queue<String> empty_queue) {
        /* Показывает безопасную работу с пустой очередью: poll/peek дают null, remove/element бросают. */
        System.out.println("peek на пустой: " + empty_queue.peek());
        System.out.println("poll на пустой: " + empty_queue.poll());
        try {
            empty_queue.remove();
        } catch (Exception empty_queue_error) {
            System.out.println("remove на пустой бросил: " + empty_queue_error.getClass().getSimpleName());
        }
        try {
            empty_queue.element();
        } catch (Exception empty_queue_error) {
            System.out.println("element на пустой бросил: " + empty_queue_error.getClass().getSimpleName());
        }
    }
}

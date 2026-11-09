/*Реализуйте FIFO waitlist через Queue: добавить, 
посмотреть следующего, извлечь, обработать пустую очередь. 
Объясните пары add/offer, remove/poll, element/peek.*/

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Queue<String> q = new ArrayDeque<>();

        // вставка в конец: add (жёстко) и offer (мягко)
        q.add("s1");
        q.offer("s2");

        // просмотр головы: element (жёстко) и peek (мягко)
        System.out.println("element: " + q.element());
        System.out.println("peek: " + q.peek());

        // извлечение головы: remove (жёстко) и poll (мягко)
        System.out.println("remove: " + q.remove());
        System.out.println("poll: " + q.poll());

        // очередь пуста — жёсткие версии бросают, мягкие возвращают null
        try { q.remove(); } catch (NoSuchElementException e) { System.out.println("remove threw"); }
        try { q.element(); } catch (NoSuchElementException e) { System.out.println("element threw"); }
        System.out.println("poll on empty: " + q.poll());
        System.out.println("peek on empty: " + q.peek());
    }
}
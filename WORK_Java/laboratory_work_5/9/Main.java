/*Создайте методы копирования из producer-списка 
в consumer-список. Объясните, почему нельзя добавлять 
конкретный объект в List<? extends Course и что разрешено 
читать из List<? super Course.*/

import java.util.*;

public class Main {
    record Course(String id) {}
    record OnlineCourse(String id) {}

    static <T> void copy(List<? extends T> src, List<? super T> dst) {
        for (T t : src) dst.add(t);
    }

    public static void main(String[] args) {
        List<Course> src = List.of(new Course("C1"), new Course("C2"));
        List<Object> dst = new ArrayList<>();
        copy(src, dst);
        System.out.println("copied: " + dst);

        List<? extends Course> producer = src;
        Course c = producer.get(0);              // читать можно как Course
        // producer.add(new Course("C3"));      // НЕЛЬЗЯ — компилятор запрещает
        System.out.println("read: " + c);

        List<? super Course> consumer = dst;
        consumer.add(new Course("C3"));         // добавлять Course можно
        Object o = consumer.get(0);             // читать — только как Object
        System.out.println("read as Object: " + o);
    }
}
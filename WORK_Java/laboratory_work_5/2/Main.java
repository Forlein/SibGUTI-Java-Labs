/*Удалите дубликаты CourseCode, сохранив первый 
порядок появления. Выберите реализацию set и объясните 
влияние equals/hashCode.*/

import java.util.*;

public class Main {
    record Course(String code, String title) {}

    public static void main(String[] args) {
        List<Course> courses = List.of(
                new Course("CS101", "Intro"),
                new Course("MA101", "Calculus"),
                new Course("CS101", "Intro v2"),
                new Course("PH101", "Physics"));

        Set<String> seen = new LinkedHashSet<>();
        List<Course> unique = new ArrayList<>();
        for (Course c : courses) if (seen.add(c.code())) unique.add(c);

        unique.forEach(System.out::println);
    }
}
package ru.sibguti.lab2.hub;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Класс EnrollmentBook - журнал заявок и закрытых дисциплин студентов.
 * Он хранит две карты: кто на какие курсы подавал заявку и какие курсы
 * студент уже закрыл. Никакой логики отказов здесь нет: журнал только
 * хранит факты, а решение принимает EnrollmentService (задание 13).
 * Зависимости: его читает и пишет CourseHubFacade при зачислении
 * (задание 15), а тесты заданий 13 и 15 проверяют его через facade.
 */
public class EnrollmentBook {

    private final Map<String, Set<String>> applications = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> completed = new HashMap<String, Set<String>>();

    /*
     * Метод hasApplication сообщает, подавал ли студент заявку на этот курс.
     * Это условие "повторная заявка" из таблицы решений задания 13.
     * Его вызывает CourseHubFacade перед применением бизнес-правил.
     */
    public boolean hasApplication(String studentId, String courseCode) {
        Set<String> studentApplications = applications.get(studentId);
        if (studentApplications == null) {
            return false;
        }
        return studentApplications.contains(courseCode);
    }

    /*
     * Метод addApplication записывает успешную заявку. Его вызывает
     * CourseHubFacade только после того, как таблица решений разрешила зачисление.
     */
    public void addApplication(String studentId, String courseCode) {
        Set<String> studentApplications = applications.get(studentId);
        if (studentApplications == null) {
            studentApplications = new HashSet<String>();
            applications.put(studentId, studentApplications);
        }
        studentApplications.add(courseCode);
    }

    /*
     * Метод hasCompletedPrerequisite проверяет, закрыл ли студент
     * предварительную дисциплину. Если prerequisite не задан, условие
     * считается выполненным, потому что его просто нет.
     * Его вызывает CourseHubFacade перед применением бизнес-правил.
     */
    public boolean hasCompletedPrerequisite(String studentId, String prerequisiteCode) {
        if (prerequisiteCode == null) {
            return true;
        }
        Set<String> studentCompleted = completed.get(studentId);
        if (studentCompleted == null) {
            return false;
        }
        return studentCompleted.contains(prerequisiteCode);
    }

    /*
     * Метод markCompleted отмечает закрытый курс студента.
     * Его вызывает импорт исторических данных и сценарии закрытия курса.
     */
    public void markCompleted(String studentId, String courseCode) {
        Set<String> studentCompleted = completed.get(studentId);
        if (studentCompleted == null) {
            studentCompleted = new HashSet<String>();
            completed.put(studentId, studentCompleted);
        }
        studentCompleted.add(courseCode);
    }
}

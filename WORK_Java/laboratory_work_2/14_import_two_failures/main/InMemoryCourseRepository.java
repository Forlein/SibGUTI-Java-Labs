package ru.sibguti.lab2.hub;

import ru.sibguti.lab2.domain.Course;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 14 без изменений.
 * Зачем он нужен в этом задании: простое хранилище в памяти с флагом недоступности для тестов отказа.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс InMemoryCourseRepository - простая реализация CourseRepository,
 * которая хранит курсы в обычном списке. Флаг unavailable позволяет
 * включить имитацию инфраструктурного отказа: методы начинают бросать
 * RepositoryUnavailableException, не меняя данные.
 * Такой флаг нужен тестам задания 15, чтобы проверить ветку инфраструктурной
 * ошибки без настоящей базы данных.
 * Зависимости: использует доменный Course и CourseCode; создаёт и настраивает
 * его CourseHubFacade, а тесты заданий 14 и 15 проверяют его поведение.
 */
public class InMemoryCourseRepository implements CourseRepository {

    private final List<Course> courses = new ArrayList<Course>();
    private boolean unavailable;

    public Optional<Course> findByCode(String normalizedCode) throws RepositoryUnavailableException {
        checkAvailable();
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            if (course.getCode().value().equals(normalizedCode)) {
                return Optional.of(course);
            }
        }
        return Optional.empty();
    }

    public void saveAll(List<Course> newCourses) throws RepositoryUnavailableException {
        checkAvailable();
        for (int i = 0; i < newCourses.size(); i++) {
            courses.add(newCourses.get(i));
        }
    }

    public int size() {
        return courses.size();
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    /*
     * Метод checkAvailable имитирует отказ хранилища: если флаг выставлен,
     * вместо данных наружу уходит RepositoryUnavailableException с cause,
     * чтобы тесты проверяли и наличие кода ошибки, и сохранение причины.
     * Его вызывают findByCode и saveAll.
     */
    private void checkAvailable() throws RepositoryUnavailableException {
        if (unavailable) {
            throw new RepositoryUnavailableException("catalog-storage недоступен, connection refused");
        }
    }
}

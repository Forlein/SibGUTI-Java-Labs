package ru.sibguti.lab2.hub;

import ru.sibguti.lab2.domain.Course;

import java.util.List;
import java.util.Optional;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 14 без изменений.
 * Зачем он нужен в этом задании: единственный способ добраться до каталога курсов, объявляет checked-отказ хранилища.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Интерфейс CourseRepository - это единственный способ добраться до каталога
 * курсов. Он намеренно объявляет checked-исключение RepositoryUnavailableException:
 * хранилище может отказать, и это внешняя причина, а не ошибка программиста.
 * Зависимости: реализацию даёт InMemoryCourseRepository, им пользуются
 * импортёр задания 14 и публичный слой CourseHubFacade задания 15.
 */
public interface CourseRepository {

    Optional<Course> findByCode(String normalizedCode) throws RepositoryUnavailableException;

    void saveAll(List<Course> courses) throws RepositoryUnavailableException;

    int size();
}

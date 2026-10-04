package ru.sibguti.lab2.hub;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 14 без изменений.
 * Зачем он нужен в этом задании: отказ хранилища, который можно повторить, поэтому он checked.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Исключение RepositoryUnavailableException - это инфраструктурный отказ
 * уровня хранилища: база или склад недоступны, соединение потеряно.
 * Оно checked, потому что причина внешняя и повторяемая, а вызывающий
 * (публичный слой CourseHubFacade) обязан превратить его в код ошибки,
 * а не пропустить к пользователю. Причина всегда уходит в cause.
 * Зависимости: его бросают реализации CourseRepository, ловит и переводит
 * в код SERVICE_UNAVAILABLE публичный слой задания 15.
 */
public class RepositoryUnavailableException extends Exception {

    private static final long serialVersionUID = 1L;

    public RepositoryUnavailableException(String message) {
        super(message);
    }

    public RepositoryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

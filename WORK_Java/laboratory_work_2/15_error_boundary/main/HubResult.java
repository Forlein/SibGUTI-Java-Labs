package ru.sibguti.lab2.hub;

import ru.sibguti.lab2.domain.ErrorCode;

/*
 * Класс HubResult - это то, что публичный слой CourseHub отдаёт наружу.
 * Он содержит признак успеха, стабильный код ошибки из ErrorCode,
 * понятное сообщение и, если был отказ, исходную причину (cause).
 * Причина наружу не показывается: она остаётся только для журнала,
 * поэтому клиент получает стабильный код и не зависит от текста исключений.
 * Зависимости: создают его методы CourseHubFacade, читают результат
 * тесты задания 15 и любой вызывающий публичный слой.
 */
public class HubResult {

    private final boolean success;
    private final ErrorCode errorCode;
    private final String message;
    private final Throwable cause;

    private HubResult(boolean success, ErrorCode errorCode, String message, Throwable cause) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
        this.cause = cause;
    }

    public static HubResult ok(String message) {
        return new HubResult(true, null, message, null);
    }

    public static HubResult error(ErrorCode errorCode, String message) {
        return new HubResult(false, errorCode, message, null);
    }

    public static HubResult error(ErrorCode errorCode, String message, Throwable cause) {
        return new HubResult(false, errorCode, message, cause);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getCause() {
        return cause;
    }

    public String toString() {
        if (success) {
            return "OK: " + message;
        }
        return "FAIL " + errorCode.code() + ": " + message;
    }
}

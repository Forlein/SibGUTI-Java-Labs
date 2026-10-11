package task_06_result_type;

import java.util.Objects;
import java.util.function.Function;

public class result<T> {
    private final T result_value;
    private final String error_message;
    private final boolean success_flag;

    private result(T result_value, String error_message, boolean success_flag) {
        /* Закрытый конструктор: объект создаётся только фабриками, чтобы инвариант не нарушался. */
        this.result_value = result_value;
        this.error_message = error_message;
        this.success_flag = success_flag;
    }

    public static <T> result<T> success(T result_value) {
        /* Фабрика успеха: гарантирует непустое значение и защищает от mix-состояний. */
        return new result<>(Objects.requireNonNull(result_value, "result_value"), null, true);
    }

    public static <T> result<T> error(String error_message) {
        /* Фабрика ошибки: гарантирует непустое сообщение. */
        return new result<>(null, Objects.requireNonNull(error_message, "error_message"), false);
    }

    public boolean is_success() {
        /* Флаг успеха; вызывающий код использует его, чтобы выбрать ветку обработки. */
        return success_flag;
    }

    public T get_value() {
        /* Возвращает значение у успеха, иначе бросает IllegalStateException. */
        if (!success_flag) throw new IllegalStateException("Нет значения: " + error_message);
        return result_value;
    }

    public String get_error() {
        /* Возвращает текст ошибки у неуспеха, иначе бросает IllegalStateException. */
        if (success_flag) throw new IllegalStateException("Ошибки нет");
        return error_message;
    }

    public T or_else(T fallback_value) {
        /* Возвращает значение или запасное при ошибке; удобно для безопасной подстановки. */
        return success_flag ? result_value : fallback_value;
    }

    public <R> result<R> map(Function<T, R> mapper) {
        /* Преобразует значение при успехе, а ошибку пробрасывает дальше без изменений. */
        return success_flag ? result.success(mapper.apply(result_value)) : result.error(error_message);
    }

    @Override
    public String toString() {
        return success_flag ? "success(" + result_value + ")" : "error(" + error_message + ")";
    }
}

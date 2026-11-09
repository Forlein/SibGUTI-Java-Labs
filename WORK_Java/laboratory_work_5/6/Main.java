/*Реализуйте Result<T> или Page<T> без raw types. Добавьте 
фабрики успеха/ошибки, инварианты и методы чтения. Покажите 
две конкретизации и compile-time защиту от смешения.*/

import java.util.*;

public class Main {
    static final class Result<T> {
        private final T value;
        private final String error;

        private Result(T value, String error) { this.value = value; this.error = error; }

        static <T> Result<T> ok(T value) {
            Objects.requireNonNull(value);
            return new Result<>(value, null);
        }
        static <T> Result<T> err(String error) {
            Objects.requireNonNull(error);
            return new Result<>(null, error);
        }

        boolean isOk() { return error == null; }

        T get() {
            if (!isOk()) throw new IllegalStateException(error);
            return value;
        }
        String error() {
            if (isOk()) throw new IllegalStateException("no error");
            return error;
        }
    }

    public static void main(String[] args) {
        Result<Integer> a = Result.ok(42);
        Result<String> b = Result.err("bad");

        System.out.println(a.get() + " / " + a.isOk());
        System.out.println(b.error() + " / " + b.isOk());

        // Result<Integer> c = Result.ok("строка"); // ошибка компиляции — защита от смешения
    }
}

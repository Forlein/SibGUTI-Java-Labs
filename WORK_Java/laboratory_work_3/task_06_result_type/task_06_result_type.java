package task_06_result_type;

public class task_06_result_type {

    public static void main(String[] args) {
        /* Задача 6: generic Result<T> без raw types — фабрики успеха/ошибки, инварианты и две конкретизации. */
        result<course> course_result = result.success(new course(1, "CS101", "Введение"));
        result<String> text_result = result.success("готово");
        result<course> error_result = result.error("Курс не найден");

        System.out.println(course_result);
        System.out.println(text_result);
        System.out.println(error_result);

        System.out.println("or_else у success: " + course_result.or_else(new course(0, "NONE", "Заглушка")));
        System.out.println("or_else у error: " + error_result.or_else(new course(0, "NONE", "Заглушка")));

        result<String> mapped_result = course_result.map(course::get_code);
        System.out.println("map в result<String>: " + mapped_result);
        /* result<course> wrong_result = result.success("строка"); */
        /* Строка выше не компилируется: тип T зафиксирован как course — это compile-time защита от смешения. */
    }
}

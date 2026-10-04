import java.util.Arrays;
import java.util.List;

/**
 * Задание 3. «Полиморфный массив».
 *
 * Главная идея: клиент renderReport(...) принимает массивы Course и
 * CourseFormatter и обходится без единого instanceof, switch или if по типу.
 * Две дополнительные реализации форматтера (Markdown, Prefixed) добавляются
 * без единой правки клиента — это и есть критерий удачной архитектуры.
 * Также показано, что смена массива на List ничего не стоит клиенту,
 * и продемонстрирована ловушка ковариантных массивов (ArrayStoreException).
 *
 * Запуск: javac 03_PolymorphicArray.java
 *         java  -cp . PolymorphicArray
 */
class PolymorphicArray {

    /**
     * Модель курса: код, название и кредиты. Неизменяемая, доступ к данным
     * только через геттеры — их вызывают все форматтеры при построении строки.
     */
    static class Course {

        private final String code;
        private final String title;
        private final int credits;

        /**
         * Создаёт курс. Вызывается из main при формировании массива courses,
         * который затем передаётся в renderReport и renderReportWithList.
         */
        Course(String code, String title, int credits) {
            this.code = code;
            this.title = title;
            this.credits = credits;
        }

        /**
         * Возвращает шифр курса. Используется CompactCourseFormatter
         * и MarkdownCourseFormatter.
         */
        String getCode() {
            return code;
        }

        /**
         * Возвращает название курса. Используется всеми форматтерами,
         * кроме PrefixedCourseFormatter, который печатает весь объект.
         */
        String getTitle() {
            return title;
        }

        /**
         * Возвращает число кредитов. Используется DetailedCourseFormatter
         * и MarkdownCourseFormatter для вывода объёма дисциплины.
         */
        int getCredits() {
            return credits;
        }

        /**
         * Переопределяет Object.toString() краткой строкой.
         * Используется в PrefixedCourseFormatter.format(...) — тот вызывает
         * course в конкатенации, и без этого метода в отчёт попал бы
         * адрес памяти.
         */
        @Override
        public String toString() {
            return code + " '" + title + "'";
        }
    }

    /**
     * Минимальный контракт форматирования: имя форматтера и метод format.
     * Минимальность важна — один-два метода легко реализовать, поэтому
     * новые форматы появляются часто.
     */
    interface CourseFormatter {

        /**
         * Возвращает короткое имя форматтера для отчёта и подписи колонки.
         * Вызывается клиентом renderReport для каждого элемента массива
         * форматтеров, а также перечисляется в main отдельным циклом.
         */
        String name();

        /**
         * Превращает курс в строку по правилам реализации.
         * Вызывается полиморфно из renderReport и renderReportWithList;
         * конкретное тело выбирает JVM по фактическому типу объекта.
         */
        String format(Course course);
    }

    /**
     * Форматтер №1: одна короткая строка с обрезкой по ширине.
     */
    static class CompactCourseFormatter implements CourseFormatter {

        private final int width;

        /**
         * Задаёт ширину обрезки. Вызывается из main при сборке массива
         * форматтеров — настройка передаётся данными, а не подклассом.
         */
        CompactCourseFormatter(int width) {
            this.width = width;
        }

        /**
         * Возвращает имя "compact" для отчёта. Вызывается клиентом
         * renderReport при формировании заголовка каждой записи.
         */
        @Override
        public String name() {
            return "compact";
        }

        /**
         * Формирует «код название», обрезая до width с многоточием.
         * Вызывается полиморфно из renderReport и из stream-версии в main.
         */
        @Override
        public String format(Course course) {
            String base = course.getCode() + " " + course.getTitle();
            return base.length() <= width ? base : base.substring(0, width - 3) + "...";
        }
    }

    /**
     * Форматтер №2: многострочный отчёт со всеми полями курса.
     */
    static class DetailedCourseFormatter implements CourseFormatter {

        /**
         * Возвращает имя "detailed" для отчёта. Вызывается клиентом
         * renderReport и используется в main для подписи колонок.
         */
        @Override
        public String name() {
            return "detailed";
        }

        /**
         * Формирует многострочное описание курса. Клиент renderReport
         * заменяет символы перевода строки на " / ", поэтому вывод
         * остаётся в одну строку.
         */
        @Override
        public String format(Course course) {
            return "course: " + course.getCode()
                    + "\n  title  : " + course.getTitle()
                    + "\n  credits: " + course.getCredits();
        }
    }

    /**
     * Форматтер №3: вывод в Markdown. Добавлен после написания клиента —
     * файл с renderReport не изменился ни одной строкой.
     */
    static class MarkdownCourseFormatter implements CourseFormatter {

        /**
         * Возвращает имя "markdown". Вызывается клиентом renderReport
         * при печати отчёта по всем курсам.
         */
        @Override
        public String name() {
            return "markdown";
        }

        /**
         * Формирует Markdown-заголовок с кодом и кредитами.
         * Вызывается полиморфно из renderReport.
         */
        @Override
        public String format(Course course) {
            return "## " + course.getTitle() + "\n"
                    + "- **code**: " + course.getCode() + "\n"
                    + "- **credits**: " + course.getCredits();
        }
    }

    /**
     * Форматтер №4: добавляет свой префикс. Демонстрирует сокрытие
     * информации: поле prefix клиенту недоступно, он умеет только
     * вызывать format(...) и name().
     */
    static class PrefixedCourseFormatter implements CourseFormatter {

        private final String prefix;

        /**
         * Задаёт префикс строки. Вызывается из main при сборке formattersV2,
         * чтобы показать, что настройка — это данные реализации.
         */
        PrefixedCourseFormatter(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Возвращает имя вместе с префиксом для отчёта. Вызывается
         * клиентом renderReport при формировании заголовка записи.
         */
        @Override
        public String name() {
            return "prefixed(" + prefix + ")";
        }

        /**
         * Возвращает префикс + Course.toString(). Неявно вызывает
         * переопределённый toString() у Course; вызывается полиморфно
         * из renderReport.
         */
        @Override
        public String format(Course course) {
            return prefix + course;
        }
    }

    /**
     * Клиент отчёта: обходит все курсы и все форматтеры без проверок по типу.
     * Вызывается из main дважды — для массива из трёх и из четырёх форматтеров,
     * — чтобы показать, что клиент не менялся. Единственная точка, где
     * происходит dynamic dispatch, — вызов f.format(course).
     */
    static String renderReport(Course[] courses, CourseFormatter[] formatters) {
        StringBuilder report = new StringBuilder();

        for (Course course : courses) {
            for (CourseFormatter f : formatters) {
                report.append("[").append(f.name()).append("] ")
                        .append(f.format(course).replace("\n", " / "))
                        .append('\n');
            }
            report.append('\n');
        }
        return report.toString();
    }

    /**
     * Тот же клиент, но принимающий List вместо массивов и меняющий
     * порядок обхода: сначала форматтеры, потом курсы.
     * Вызывается из main с результатами Arrays.asList(...) и показывает,
     * что клиент работает с абстракцией List, а не с конкретным ArrayList.
     */
    static String renderReportWithList(List<Course> courses, List<CourseFormatter> formatters) {
        StringBuilder report = new StringBuilder();
        for (CourseFormatter f : formatters) {
            for (Course course : courses) {
                report.append(f.format(course).replace("\n", " / ")).append('\n');
            }
            report.append("----\n");
        }
        return report.toString();
    }

    /**
     * Точка входа: собирает данные, вызывает оба варианта клиента,
     * показывает потоковую реализацию того же обхода, перечисляет имена
     * форматтеров через контракт и воспроизводит ArrayStoreException
     * на ковариантном массиве.
     */
    public static void main(String[] args) {

        Course[] courses = {
                new Course("SIBGUTI-101", "Основы Java", 6),
                new Course("SIBGUTI-202", "Сетевые технологии", 4)
        };

        CourseFormatter[] formatters = {
                new CompactCourseFormatter(24),
                new DetailedCourseFormatter(),
                new MarkdownCourseFormatter()
        };

        System.out.println("=== Отчёт: 2 курса x 3 форматтера, ноль if-ов ===");
        System.out.print(renderReport(courses, formatters));

        System.out.println("=== Добавили PrefixedCourseFormatter: клиент не тронут ===");
        CourseFormatter[] formattersV2 = {
                new CompactCourseFormatter(24),
                new DetailedCourseFormatter(),
                new MarkdownCourseFormatter(),
                new PrefixedCourseFormatter(">> ")
        };
        System.out.print(renderReport(courses, formattersV2));

        List<Course> courseList = Arrays.asList(courses);
        List<CourseFormatter> formatterList = Arrays.asList(formattersV2);
        System.out.println("=== То же самое через List (а не массив) ===");
        System.out.print(renderReportWithList(courseList, formatterList));

        System.out.println("=== Реализация на stream API ===");
        String oneLiner = String.join(" | ",
                Arrays.stream(courses)
                        .map(c -> new CompactCourseFormatter(18).format(c))
                        .collect(java.util.stream.Collectors.toList()));
        System.out.println(oneLiner);

        System.out.println();
        System.out.println("=== Имена форматтеров (работа через интерфейс) ===");
        for (CourseFormatter f : formattersV2) {
            System.out.println(" - " + f.name());
        }

        System.out.println();
        System.out.println("=== Ловушка ковариантных массивов ===");
        Object[] trick = new CompactCourseFormatter[1];
        try {
            trick[0] = new DetailedCourseFormatter();
            System.out.println("не должно сюда попасть");
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== ИТОГ ===");
        System.out.println("renderReport(Course[], CourseFormatter[]):");
        System.out.println("  • не содержит ни instanceof, ни switch, ни if по типу;");
        System.out.println("  • не упоминает ни одного конкретного форматтера;");
        System.out.println("  • вырос с 3 до 4 реализаций без единой правки.");
    }
}

/**
 * Задание 1. «Один контракт, две реализации».
 *
 * Демонстрирует базовую идею ООП: описать поведение ИНТЕРФЕЙСОМ
 * (CourseFormatter) и дать несколько независимых реализаций. Клиент
 * работает только с интерфейсным типом, поэтому добавление новой
 * реализации не требует ни одной правки клиентского кода.
 *
 * Запуск: javac 01_OneContractTwoImplementations.java
 *         java  -cp . OneContractTwoImplementations
 *
 * Классы вложенные и package-private, потому что имя класса в Java
 * не может начинаться с цифры, а файл называется 01_...; вложенность
 * гарантирует отсутствие пересечения имён с файлами заданий 2...15.
 */
class OneContractTwoImplementations {

    /**
     * Модель данных курса: неизменяемый POJO с private final полями.
     * Отдаёт данные наружу только через геттеры, чтобы состояние
     * нельзя было подменить извне.
     */
    static class Course {

        private final String code;
        private final String title;
        private final int credits;
        private final boolean mandatory;

        /**
         * Создаёт курс и фиксирует его поля.
         * Ключевое слово this отличает поле объекта от одноимённого
         * параметра метода — без него присваивание было бы неоднозначным.
         * Вызывается напрямую из main, где создаются тестовые данные.
         */
        Course(String code, String title, int credits, boolean mandatory) {
            this.code = code;
            this.title = title;
            this.credits = credits;
            this.mandatory = mandatory;
        }

        /**
         * Возвращает шифр курса.
         * Используется обоими форматтерами (CompactCourseFormatter,
         * DetailedCourseFormatter) и анонимной реализацией в main —
         * им нужен короткий идентификатор курса для вывода.
         */
        String getCode() {
            return code;
        }

        /**
         * Возвращает название курса.
         * Используется обоими форматтерами для построения строки
         * представления: компактного и подробного.
         */
        String getTitle() {
            return title;
        }

        /**
         * Возвращает количество кредитов.
         * Используется DetailedCourseFormatter и анонимной реализацией
         * в main для вывода объёма курса; CompactCourseFormatter
         * не вызывает его — там важна только длина итоговой строки.
         */
        int getCredits() {
            return credits;
        }

        /**
         * Возвращает признак обязательности дисциплины.
         * Именно этот метод вызывает DetailedCourseFormatter, чтобы
         * напечатать «да/нет». Имя isXxx() — соглашение для boolean,
         * а не требование языка.
         */
        boolean isMandatory() {
            return mandatory;
        }

        /**
         * Переопределяет Object.toString() ради читаемого вывода вместо
         * «Course@1b6d3586». Используется при печати объектов в main.
         * Аннотация @Override заставляет компилятор проверить, что
         * сигнатура действительно совпадает с методом родителя.
         */
        @Override
        public String toString() {
            return code + " '" + title + "' (" + credits + " cr)"
                    + (mandatory ? ", mandatory" : ", optional");
        }
    }

    /**
     * Контракт форматирования: «у меня есть курс — дай мне строку».
     * Единственный метод не имеет тела — это описание ЧТО делать,
     * а КАК именно решает каждая реализация.
     */
    interface CourseFormatter {

        /**
         * Превращает курс в строку по правилам конкретной реализации.
         * Вызывается из main через ссылки статического типа
         * CourseFormatter; dynamic dispatch внутри JVM выбирает тело
         * CompactCourseFormatter или DetailedCourseFormatter.
         */
        String format(Course course);
    }

    /**
     * Реализация №1: короткая однострочная строка, обрезанная до width.
     * Создаётся в main и хранится в переменной типа CourseFormatter.
     */
    static class CompactCourseFormatter implements CourseFormatter {

        private final int width;

        /**
         * Задаёт максимальную ширину строки.
         * Вызывается из main при создании форматтера: так демонстрируется,
         * что настройка реализации передаётся данными, а не подклассом.
         */
        CompactCourseFormatter(int width) {
            this.width = width;
        }

        /**
         * Формирует «шифр — название» и обрезает до width с многоточием.
         * Вызывается полиморфно из main для обоих курсов, чтобы показать,
         * что одинаковый статический тип даёт разное поведение.
         */
        @Override
        public String format(Course course) {
            String base = course.getCode() + " — " + course.getTitle();
            if (base.length() <= width) {
                return base;
            }
            return base.substring(0, width - 3) + "...";
        }
    }

    /**
     * Реализация №2: подробный многострочный отчёт по всем полям курса.
     * Не имеет ничего общего с CompactCourseFormatter, кроме implements
     * CourseFormatter — именно это и обеспечивает полиморфизм.
     */
    static class DetailedCourseFormatter implements CourseFormatter {

        /**
         * Собирает многострочное описание курса через StringBuilder.
         * Вызывается из main; вспомогательный метод oneLine() затем
         * сокращает результат до первой строки, чтобы вывод не расползался.
         */
        @Override
        public String format(Course course) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(course.getCode()).append(" ===").append('\n');
            sb.append("Название : ").append(course.getTitle()).append('\n');
            sb.append("Кредиты  : ").append(course.getCredits()).append('\n');
            sb.append("Обязат.  : ").append(course.isMandatory() ? "да" : "нет").append('\n');
            return sb.toString();
        }
    }

    /**
     * Точка входа: создаёт данные, две реализации и анонимную третью,
     * затем сравнивает статический тип переменной (то, что видит
     * компилятор) с фактическим типом объекта (то, что выбирает JVM).
     * Все вызовы format() идут через переменные типа CourseFormatter.
     */
    public static void main(String[] args) {

        Course java = new Course("SIBGUTI-101", "Основы Java", 6, true);
        Course net = new Course("SIBGUTI-202", "Сетевые технологии", 4, false);

        CourseFormatter compact = new CompactCourseFormatter(28);
        CourseFormatter detailed = new DetailedCourseFormatter();

        System.out.println("=== Вызовы через переменные ИНТЕРФЕЙСНОГО типа ===");
        System.out.println(compact.format(java));
        System.out.println(detailed.format(net));

        System.out.println();
        System.out.println("=== Тот же контракт, разные объекты ===");
        System.out.println(compact.format(net));
        System.out.println(detailed.format(java));

        System.out.println();
        System.out.println("=== Статический тип (то, что видит компилятор) ===");
        System.out.println("compact  объявлен как: " + CourseFormatter.class.getSimpleName());
        System.out.println("detailed объявлен как: " + CourseFormatter.class.getSimpleName());

        System.out.println();
        System.out.println("=== Фактический тип (то, что выбирается во время выполнения) ===");
        System.out.println("compact  -> объект класса " + compact.getClass().getSimpleName());
        System.out.println("detailed -> объект класса " + detailed.getClass().getSimpleName());

        System.out.println();
        System.out.println("=== Вызов format() дал РАЗНЫЕ результаты при одном статическом типе ===");
        System.out.println("compact.format(java)  = " + compact.format(java));
        System.out.println("detailed.format(java) = " + oneLine(detailed.format(java)));

        System.out.println();
        System.out.println("=== Новая реализация — клиент не изменился ===");
        CourseFormatter loudFormatter = new CourseFormatter() {
            /**
             * Анонимная реализация контракта «на месте», без отдельного класса.
             * Вызывается из main сразу после создания, чтобы доказать:
             * клиент не знает имён конкретных форматтеров.
             */
            @Override
            public String format(Course course) {
                return ">>> " + course.getCode() + " / " + course.getTitle()
                        + " / " + course.getCredits() + " cr <<<";
            }
        };
        System.out.println(loudFormatter.format(java));
    }

    /**
     * Оставляет только первую строку многострочного текста и добавляет
     * маркер «[...]». Вызывается из main для результата
     * detailed.format(...), чтобы сравнение двух форматов помещалось
     * в одну строку вывода.
     */
    private static String oneLine(String text) {
        int eol = text.indexOf('\n');
        return eol < 0 ? text : text.substring(0, eol) + " [...]";
    }
}

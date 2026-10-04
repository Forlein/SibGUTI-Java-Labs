/**
 * Задание 12. «Sealed-иерархия результата».
 *
 * Задача файла: объявить закрытый интерфейс EnrollmentResult с разрешёнными
 * подтипами Accepted, Rejected и WaitListed, обработать все варианты
 * исчерпывающим switch без default и показать, где именно компилятор
 * потребовал изменения при добавлении нового подтипа.
 *
 * Зачем sealed: у открытого интерфейса любая внешняя сторона может
 * реализовать его, поэтому компилятор не может проверить полноту обработки.
 * Ключевое слово permits перечисляет разрешённые подтипы, откуда следуют
 * два свойства: проверка исчерпывающего switch и контролируемая
 * расширяемость — добавить подтип может только автор иерархии.
 * Подтипы sealed обязаны быть final, sealed или non-sealed, иначе закрытость
 * не работает. Record всегда final, поэтому требование выполнено автоматически.
 *
 * Запуск: javac --release 21 12_SealedHierarchyResult.java
 *         java -cp . SealedHierarchyResult
 *
 * Требования к среде: JDK 21, поскольку switch по типам (pattern matching
 * for switch) появился в Java 21. Sealed-интерфейсы и record нужны Java 16+,
 * pattern instanceof — Java 16+. В IntelliJ IDEA или VS Code достаточно
 * выставить Language level = 21. Остальные задания файлов 01...11 и 13...15
 * собираются на JDK 8 и новее.
 */
class SealedHierarchyResult {

    /**
     * Закрытый интерфейс результата операции зачисления.
     * Ключевое слово permits перечисляет разрешённые подтипы; без него
     * интерфейс считался бы открытым. Объявлен перед interface и перед class.
     * Подтипы объявлены рядом как вложенные типы для удобства чтения, но
     * главное условие — они находятся в том же пакете.
     */
    sealed interface EnrollmentResult
            permits SealedHierarchyResult.Accepted,
                    SealedHierarchyResult.Rejected,
                    SealedHierarchyResult.WaitListed {

        /**
         * Метод «умеет объяснить себя сам»: альтернатива switch, при которой
         * не нужно писать обработку в каждом классе. Вызывается напрямую
         * из renderSelf(...), а также внутри render(...), renderIfElse(...)
         * и record-подтипов.
         */
        String headline();
    }

    /**
     * Зачислен. Record — класс-значение с авто-генерируемыми
     * equals, hashCode и toString. Все поля final, поэтому record всегда final
     * и требование sealed-иерархии выполнено автоматически.
     * Создаётся сервисом при наличии свободного места.
     */
    record Accepted(String studentId, String courseCode) implements EnrollmentResult {

        /**
         * Возвращает текст результата с идентификатором и кодом курса.
         * Вызывается обработчиками render, renderIfElse и renderSelf,
         * а также напрямую клиентом.
         */
        @Override
        public String headline() {
            return "Зачислен: " + studentId + " -> " + courseCode;
        }
    }

    /**
     * Отказ с указанием причины. Создаётся сервисом при непрохождении
     * проверок, поэтому этот вариант клиент обязан отличать от листа ожидания.
     */
    record Rejected(String studentId, String reason) implements EnrollmentResult {

        /**
         * Возвращает текст результата с идентификатором и причиной отказа.
         * Вызывается теми же обработчиками, что и headline() у Accepted.
         */
        @Override
        public String headline() {
            return "Отказ: " + studentId + " (" + reason + ")";
        }
    }

    /**
     * Лист ожидания с номером позиции. Создаётся сервисом, когда свободных
     * мест нет; позиция хранится прямо в record, поэтому клиенту не нужен
     * отдельный запрос за ней.
     */
    record WaitListed(String studentId, int position) implements EnrollmentResult {

        /**
         * Возвращает текст результата с идентификатором и позицией в листе
         * ожидания. Вызывается теми же обработчиками, что и headline()
         * у Accepted.
         */
        @Override
        public String headline() {
            return "Лист ожидания: " + studentId + ", позиция " + position;
        }
    }

    /**
     * Главная обработка: switch-выражение (Java 14+) с pattern matching
     * по типам (Java 21). Значения можно присваивать, каждая ветка
     * завершается стрелкой, а отсутствие default позволяет компилятору
     * проверить исчерпывающее покрытие всех подтипов из permits.
     * Вызывается из main для каждого результата и в цикле сравнения
     * трёх способов обработки.
     */
    static String render(EnrollmentResult result) {
        return switch (result) {
            case Accepted a -> "OK      | " + a.headline();
            case Rejected r -> "FAIL    | " + r.headline();
            case WaitListed w -> "WAIT    | " + w.headline() + ", ждать освобождения места";
        };
    }

    /**
     * Второй способ обработки: превратить результат в строку отчёта,
     * вызвав метод, объявленный в интерфейсе. Работает без switch вовсе,
     * но полноту обработки компилятор не проверяет — это цена гибкости.
     * Вызывается из main в цикле сравнения способов обработки.
     */
    static String renderSelf(EnrollmentResult result) {
        return result.headline();
    }

    /**
     * Третий способ: pattern instanceof (Java 16+), устаревший стиль.
     * Он умеет проверять тип и связывать переменную, но гарантии полноты
     * не даёт. Вызывается из main в цикле сравнения способов обработки.
     */
    static String renderIfElse(EnrollmentResult result) {
        if (result instanceof Accepted a) {
            return "OK      | " + a.headline();
        } else if (result instanceof Rejected r) {
            return "FAIL    | " + r.headline();
        } else if (result instanceof WaitListed w) {
            return "WAIT    | " + w.headline();
        }
        throw new IllegalStateException("неизвестный результат");
    }

    /**
     * Минимальная модель кандидата для демонстрации всех трёх исходов
     * сервиса зачисления.
     */
    static class Student {

        final String id;
        final int age;

        /**
         * Сохраняет идентификатор и возраст. Вызывается из main при создании
         * четырёх студентов: совершеннолетнего, несовершеннолетнего и двоих,
         * которым мест не хватит.
         */
        Student(String id, int age) {
            this.id = id;
            this.age = age;
        }
    }

    /**
     * Минимальная модель дисциплины. Вместимость курса ограничивает число
     * зачислений, поэтому с её помощью сервис воспроизводит все три исхода.
     */
    static class Course {

        final String code;
        final int capacity;

        /**
         * Сохраняет код и вместимость курса. Вызывается из main
         * при создании единственного курса с одним местом.
         */
        Course(String code, int capacity) {
            this.code = code;
            this.capacity = capacity;
        }
    }

    /**
     * Сервис зачисления, возвращающий sealed-тип вместо boolean: boolean
     * не позволяет отличить отказ от листа ожидания, и клиенту пришлось бы
     * заводить отдельный метод или флаг. Возвращает все три разрешённых
     * подтипа EnrollmentResult.
     */
    static class EnrollmentService {

        private final Course course;
        private int enrolledCount;
        private int waitListCount;

        /**
         * Сохраняет курс, по которому принимаются решения. Вызывается из main
         * при создании единственного сервиса.
         */
        EnrollmentService(Course course) {
            this.course = course;
        }

        /**
         * Единственный метод сервиса: проверяет возраст, затем наличие мест
         * и возвращает Accepted, Rejected или WaitListed. Побочные эффекты —
         * счётчики зачисленных и ожидающих. Вызывается из main четыре раза,
         * результаты складываются в массив и обрабатываются общим кодом.
         */
        EnrollmentResult enroll(Student student) {
            if (student.age < 18) {
                return new Rejected(student.id, "возраст меньше 18");
            }
            if (enrolledCount < course.capacity) {
                enrolledCount++;
                return new Accepted(student.id, course.code);
            }
            waitListCount++;
            return new WaitListed(student.id, waitListCount);
        }

        /**
         * Возвращает число зачисленных. Вызывается из main для печати
         * итогов первого эксперимента.
         */
        int getEnrolledCount() {
            return enrolledCount;
        }
    }

    /**
     * Точка входа: получает три исхода от сервиса, сравнивает три способа
     * обработки одного и того же результата, показывает диагностику
     * компилятора при неполном switch, пошагово разбирает добавление
     * нового подтипа и перечисляет области применения sealed-иерархий.
     */
    public static void main(String[] args) {

        System.out.println("=== 1. СЕРВИС ВОЗВРАЩАЕТ ВСЕ ТРИ ВАРИАНТА ===");
        Course course = new Course("CRS-101", 1);
        EnrollmentService service = new EnrollmentService(course);

        Student adult = new Student("STU-1", 20);
        Student minor = new Student("STU-2", 16);
        Student second = new Student("STU-3", 22);
        Student third = new Student("STU-4", 25);

        EnrollmentResult[] results = {
                service.enroll(adult),
                service.enroll(minor),
                service.enroll(second),
                service.enroll(third)
        };

        for (EnrollmentResult r : results) {
            System.out.println("   " + render(r));
        }
        System.out.println("   зачислено: " + service.getEnrolledCount() + ", мест: " + course.capacity);

        System.out.println();
        System.out.println("=== 2. ТРИ СПОСОБА ОБРАБОТКИ ОДНИХ И ТЕХ ЖЕ ДАННЫХ ===");
        for (EnrollmentResult r : results) {
            System.out.println("   switch : " + render(r));
            System.out.println("   if/else: " + renderIfElse(r));
            System.out.println("   self   : " + renderSelf(r));
            System.out.println("   фактический тип: " + r.getClass().getSimpleName()
                    + ", это record? " + r.getClass().isRecord());
            System.out.println("   ---");
        }

        System.out.println();
        System.out.println("=== 3. ЗАЧЕМ SEALED: ЧТО ПРОВЕРЯЕТ КОМПИЛЯТОР ===");
        System.out.println("   Список permitted-типов известен компилятору:");
        System.out.println("   для EnrollmentResult их ровно три: Accepted, Rejected, WaitListed.");
        System.out.println("   Попробуйте УДАЛИТЬ одну строку case из render(...) —");
        System.out.println("   компилятор ответит:");
        System.out.println();
        System.out.println("       error: the switch expression does not cover all possible input values");
        System.out.println();
        System.out.println("   Без sealed (с обычным interface) такой ошибки не было бы:");
        System.out.println("   switch по обычному интерфейсу требует default, а default");
        System.out.println("   отключает проверку полноты. То есть sealed ПОЗВОЛЯЕТ");
        System.out.println("   отказаться от default и получить проверку компилятора.");

        System.out.println();
        System.out.println("=== 4. КАК ДОБАВИТЬ НОВЫЙ ВАРИАНТ (шаг 3 задания) ===");
        System.out.println("   Шаг A. Добавить подтип в permits:");
        System.out.println("       sealed interface EnrollmentResult");
        System.out.println("           permits Accepted, Rejected, WaitListed, Deferred { ... }");
        System.out.println();
        System.out.println("   Шаг B. Объявить сам подтип (обязан быть final/sealed/non-sealed):");
        System.out.println("       record Deferred(String studentId, String reason) implements EnrollmentResult {");
        System.out.println("           public String headline() { return \"Отложено: \" + reason; }");
        System.out.println("       }");
        System.out.println();
        System.out.println("   Шаг C. ВЕРИФИКАЦИЯ: после шагов A и B компилятор УКАЖЕТ");
        System.out.println("   ровно на то место, которое пришлось забыть:");
        System.out.println();
        System.out.println("       SealedHierarchyResult.java:NNN");
        System.out.println("           return switch (result) {");
        System.out.println("       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("       error: the switch expression does not cover all possible input values");
        System.out.println("   missing:   case Deferred _ ->");
        System.out.println();
        System.out.println("   Обратите внимание на ДВА сообщения компилятора: где ошибка");
        System.out.println("   и КАКОГО случая не хватает. Это самая полезная диагностика,");
        System.out.println("   которую дают sealed-типы и pattern switch.");
        System.out.println();
        System.out.println("   Альтернатива «выключить проверку»: добавить default ->");
        System.out.println("   компиляция снова пройдёт, но вы потеряете гарантию полноты.");
        System.out.println("   В закрытой иерархии это почти всегда ошибка проектирования,");
        System.out.println("   и на ревью её ловят именно по отсутствию default.");
        System.out.println();
        System.out.println("   АЛЬТЕРНАТИВНЫЙ (хуже) СПОСОБ: non-sealed.");
        System.out.println("   Если объявить  interface DeferredBase extends EnrollmentResult");
        System.out.println("   как non-sealed, иерархия перестанет быть полностью закрытой,");
        System.out.println("   и компилятор потребует default — иначе исчерпывающий switch");
        System.out.println("   невозможен (состояние «открытое»). Именно поэтому");
        System.out.println("   «полностью закрытая иерархия» и «открытое место для");
        System.out.println("   расширения» — это разные проектировочные решения.");

        System.out.println();
        System.out.println("=== 5. ГДЕ ЭТО ПОЛЕЗНО ===");
        System.out.println("   + Результат операции с небольшим закрытым набором исходов:");
        System.out.println("     результат запроса, результат валидации, результат операции.");
        System.out.println("   + Когда набор исходов ИЗВЕСТЕН и меняется редко.");
        System.out.println("   - Когда набор открыт (плагины, внешние команды): нужен обычный");
        System.out.println("     интерфейс и visitor/регистрация (см. задание 15).");
    }
}
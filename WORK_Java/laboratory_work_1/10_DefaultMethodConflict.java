/**
 * Задание 10. «Конфликт default-методов».
 *
 * Задача файла: создать два интерфейса с одинаковым default-методом,
 * показать возникающую ошибку компиляции и продемонстрировать три способа
 * разрешить конфликт, включая явное переопределение и вызов
 * Интерфейс.super.метод(...).
 *
 * Запуск: javac 10_DefaultMethodConflict.java
 *         java -cp . DefaultMethodConflict
 *
 * Требования к среде: JDK 8+, поскольку default-методы появились в Java 8.
 */
class DefaultMethodConflict {

    /**
     * Первый интерфейс: документ можно описать для аудита.
     * default-метод существует для того, чтобы новый метод можно было добавить
     * в интерфейс без правки всех существующих реализаций, в том числе чужих.
     */
    interface Auditable {

        /** Обязательная часть контракта: идентификатор для аудита. */
        String auditId();

        /**
         * Метод по умолчанию: тело есть, поэтому реализовывать его необязательно.
         * Вызывается в DocumentWithExplicitOverride и RichReportable
         * через форму Интерфейс.super, а также напрямую у любой реализации,
         * которая его не переопределила.
         */
        default String describeForAudit() {
            return "AUDIT[" + auditId() + "]";
        }
    }

    /**
     * Второй интерфейс: документ можно показать в отчёте.
     * Содержит default-метод с той же сигнатурой, что и у Auditable, но с другим
     * телом — источник неоднозначности «ромба» в множественном наследовании
     * поведения.
     */
    interface Reportable {

        /** Обязательная часть контракта: заголовок отчёта. */
        String reportTitle();

        /**
         * Конфликтующий метод по умолчанию: то же имя и сигнатура, что
         * describeForAudit() в Auditable, но другое тело. Пока оба интерфейса
         * реализуются напрямую и метод не переопределён, компилятор считает
         * выбор невозможным. Вызывается из DocumentWithExplicitOverride
         * и RichReportable через форму Интерфейс.super.
         */
        default String describeForAudit() {
            return "REPORT[" + reportTitle() + "]";
        }
    }

    /**
     * Базовый класс: метод суперкласса всегда сильнее метода интерфейса,
     * поэтому конфликт разрешается автоматически по правилам спецификации.
     * Вызывается унаследованным describeForAudit() в DocumentFromClass.
     */
    static class DocumentBase {

        /**
         * Реализация метода в классе: именно она будет вызвана у потомков,
         * даже если те реализуют оба интерфейса. Вызывается из DocumentFromClass.
         */
        public String describeForAudit() {
            return "BASE-DOC[описание из базового класса]";
        }
    }

    /**
     * Документ, получающий метод по правилу приоритета суперкласса.
     * Дублировать код не требуется, но выбрать собственное поведение
     * таким способом нельзя: доступна только версия из класса-родителя.
     */
    static class DocumentFromClass extends DocumentBase implements Auditable, Reportable {

        private final String id;
        private final String title;

        /**
         * Сохраняет идентификатор и заголовок документа. Вызывается из main
         * при создании объекта для второго эксперимента.
         */
        DocumentFromClass(String id, String title) {
            this.id = id;
            this.title = title;
        }

        /**
         * Возвращает идентификатор документа. Вызывается из
         * default-метода describeForAudit() интерфейса Auditable.
         */
        @Override
        public String auditId() {
            return id;
        }

        /**
         * Возвращает заголовок документа. Вызывается из
         * default-метода describeForAudit() интерфейса Reportable.
         */
        @Override
        public String reportTitle() {
            return title;
        }
    }

    /**
     * Документ, где конфликт разрешается явно: класс переопределяет метод
     * и через форму Интерфейс.super вызывает обе версии. Эта форма разрешена
     * только в классе, который напрямую реализует данный интерфейс.
     */
    static class DocumentWithExplicitOverride implements Auditable, Reportable {

        private final String id;
        private final String title;

        /**
         * Сохраняет идентификатор и заголовок документа. Вызывается из main
         * при создании объекта для третьего эксперимента.
         */
        DocumentWithExplicitOverride(String id, String title) {
            this.id = id;
            this.title = title;
        }

        /**
         * Возвращает идентификатор документа. Вызывается из
         * describeForAudit() этого класса при обращении к Auditable.super.
         */
        @Override
        public String auditId() {
            return id;
        }

        /**
         * Возвращает заголовок документа. Вызывается из
         * describeForAudit() этого класса при обращении к Reportable.super.
         */
        @Override
        public String reportTitle() {
            return title;
        }

        /**
         * Разрешение конфликта: аннотация означает, что переопределяются
         * методы по умолчанию обоих интерфейсов. Вызывает Auditable.super
         * и Reportable.super, а решение о том, что делать с двумя ответами,
         * принимает сам программист. Вызывается из main.
         */
        @Override
        public String describeForAudit() {
            String fromAudit = Auditable.super.describeForAudit();
            String fromReport = Reportable.super.describeForAudit();
            return fromAudit + " + " + fromReport;
        }
    }

    /**
     * Интерфейс, который одновременно наследует Auditable и Reportable,
     * поэтому он является подтипом обоих и его метод по умолчанию побеждает:
     * неоднозначности не возникает. Одного лишь extends Reportable было бы
     * недостаточно, Auditable и этот интерфейс остались бы несравнимыми.
     */
    interface RichReportable extends Auditable, Reportable {

        /**
         * Переопределяет методы обоих родителей и объединяет их поведение;
         * вызовы Интерфейс.super корректны в интерфейсе. Используется
         * реализацией RichDocument, поэтому конфликт там не возникает.
         */
        @Override
        default String describeForAudit() {
            return "RICH-REPORT[" + reportTitle() + "]: "
                    + Auditable.super.describeForAudit()
                    + " + " + Reportable.super.describeForAudit();
        }
    }

    /**
     * Документ, которому достаточно реализовать один интерфейс:
     * конфликт уже разрешён на уровне иерархии интерфейсов.
     */
    static class RichDocument implements RichReportable {

        private final String id;
        private final String title;

        /**
         * Сохраняет идентификатор и заголовок документа. Вызывается из main
         * при создании объекта для четвёртого эксперимента.
         */
        RichDocument(String id, String title) {
            this.id = id;
            this.title = title;
        }

        /**
         * Возвращает идентификатор документа. Вызывается из
         * describeForAudit() интерфейса Auditable по цепочке наследования.
         */
        @Override
        public String auditId() {
            return id;
        }

        /**
         * Возвращает заголовок документа. Вызывается из describeForAudit()
         * интерфейса Reportable и самого RichReportable.
         */
        @Override
        public String reportTitle() {
            return title;
        }
    }

    /**
     * Интерфейс закрываемого отчёта. Приведён как пример применения
     * методов по умолчанию без конфликта: реализациям не нужно писать
     * одинаковые вспомогательные методы.
     */
    interface Report extends AutoCloseable {

        /** Обязательное действие: реализация должна научиться публиковать отчёт. */
        void publish();

        /** Обязательное действие: реализация должна научиться закрываться. */
        @Override
        void close();

        /**
         * Обёртка «закрыть, проглотив исключение»: тело одинаково для всех
         * реализаций, поэтому и живёт в интерфейсе. Вызывается из main,
         * чтобы показать, что метод получен без написания кода в реализации.
         */
        default void closeQuietly() {
            try {
                close();
            } catch (Exception e) {
            }
        }

        /**
         * Шаблон «применить ко всем»: публикует каждый переданный отчёт,
         * затем собственный. Вызовы dispatch-ятся по типу каждого аргумента,
         * поэтому достаточно знать только интерфейс Report. Вызывается из main.
         */
        default void publishAll(Report[] others) {
            for (Report other : others) {
                other.publish();
            }
            publish();
        }
    }

    /**
     * Конкретная реализация отчёта, печатающего действия в консоль.
     * Методы по умолчанию интерфейса она не реализует и получает бесплатно.
     */
    static class ConsoleReport implements Report {

        private final String name;

        /**
         * Запоминает имя отчёта для печати в publish() и close().
         * Вызывается из main при создании двух отчётов.
         */
        ConsoleReport(String name) {
            this.name = name;
        }

        /**
         * Печатает факт публикации отчёта. Вызывается из publishAll(...)
         * интерфейса Report для каждого аргумента и для самого объекта.
         */
        @Override
        public void publish() {
            System.out.println("         публикую отчёт: " + name);
        }

        /**
         * Печатает факт закрытия отчёта. Вызывается напрямую из closeQuietly()
         * интерфейса Report.
         */
        @Override
        public void close() {
            System.out.println("         закрываю отчёт: " + name);
        }
    }

    /**
     * Точка входа: показывает ошибку компиляции при наследовании двух
     * несравнимых версий метода, затем три способа разрешения конфликта,
     * разницу между наследованием поведения и состояния, порядок разрешения
     * по спецификации и пользу closeQuietly() и publishAll(...).
     */
    public static void main(String[] args) {

        System.out.println("=== 1. ШАГ 1: ПОЛУЧАЕМ ОШИБКУ КОМПИЛЯЦИИ ===");
        System.out.println("   Класс ниже НАМЕРЕННО закомментирован, потому что");
        System.out.println("   он не компилируется:");
        System.out.println();
        System.out.println("       static class BrokenDocument implements Auditable, Reportable {");
        System.out.println("           public String auditId()    { return \"A-1\"; }");
        System.out.println("           public String reportTitle() { return \"отчёт\"; }");
        System.out.println("           // describeForAudit() НЕ переопределяем!");
        System.out.println("       }");
        System.out.println();
        System.out.println("   Что скажет javac:");
        System.out.println("       BrokenDocument inherits unrelated defaults for describeForAudit()");
        System.out.println("         from Auditable and Reportable");
        System.out.println("       error: incompatible types: BrokenDocument cannot be converted to Auditable");
        System.out.println();
        System.out.println("   Смысл сообщения: класс наследует ДВА РАЗНЫХ тела одного метода,");
        System.out.println("   и ни одно из них не является «более специфичным».");
        System.out.println("   Угадать, какое использовать, невозможно — значит, выбирать должен программист.");

        System.out.println();
        System.out.println("=== 2. ШАГ 2: РАЗРЕШЕНИЕ 1 — приоритет класса-родителя ===");
        DocumentFromClass d1 = new DocumentFromClass("DOC-1", "Отчёт за 2026");
        System.out.println("   " + d1.describeForAudit());
        System.out.println("   Метод пришёл из DocumentBase, конфликт разрешён правилом JLS,");
        System.out.println("   дублирующего код не потребовалось.");

        System.out.println();
        System.out.println("=== 3. ШАГ 2: РАЗРЕШЕНИЕ 2 — явный override (главный) ===");
        DocumentWithExplicitOverride d2 =
                new DocumentWithExplicitOverride("DOC-2", "Отчёт за 2026");
        System.out.println("   " + d2.describeForAudit());
        System.out.println("   Внутри вызваны ОБА варианта через Интерфейс.super.метод(...),");
        System.out.println("   а решение «что делать с двумя ответами» принял программист.");

        System.out.println();
        System.out.println("=== 4. ШАГ 2: РАЗРЕШЕНИЕ 3 — более специфичный интерфейс ===");
        RichDocument d3 = new RichDocument("DOC-3", "Богатый отчёт");
        System.out.println("   " + d3.describeForAudit());
        System.out.println("   RichReportable extends Auditable, Reportable — то есть он");
        System.out.println("   подтип ОБОИХ, поэтому его default-метод «побеждает» и");
        System.out.println("   неоднозначности не возникает. Сам факт extends у одного");
        System.out.println("   интерфейса не «победил» бы Auditable — нужна связь с обоими.");

        System.out.println();
        System.out.println("=== 5. ПОЛЯ: default-методы не дают множественного наследования состояния ===");
        System.out.println("   Поля в интерфейсе всегда public static final — это КОНСТАНТЫ.");
        System.out.println("   Приватного состояния у интерфейса быть не может, поэтому:");
        System.out.println("     • поведение наследовать можно (default-методы);");
        System.out.println("     • состояние — нельзя, только через композицию (задание 7).");

        System.out.println();
        System.out.println("=== 6. ПРАВИЛА РАЗРЕШЕНИЯ (шпаргалка из спецификации) ===");
        System.out.println("   1) Метод из СУПЕРКЛАССА  >  default-метод интерфейса;");
        System.out.println("   2) default-метод БОЛЕЕ СПЕЦИФИЧНОГО интерфейса");
        System.out.println("        (тот, что стоит «ближе» к классу по extends) > более общий;");
        System.out.println("   3) иначе — КОНФЛИКТ: ошибка компиляции;");
        System.out.println("   4) разрешить можно явным переопределением в классе,");
        System.out.println("      в том числе вызовом Интерфейс.super.метод(...).");

        System.out.println();
        System.out.println("=== 7. ПОЛЕЗНОЕ ПРИМЕНЕНИЕ: closeQuietly() из интерфейса ===");
        Report r1 = new ConsoleReport("R-1");
        Report r2 = new ConsoleReport("R-2");
        r1.publishAll(new Report[]{r2});
        r1.closeQuietly();
        System.out.println("         closeQuietly() не бросил исключение, хотя close() мог бы");

        System.out.println();
        System.out.println("=== 8. ИТОГ ===");
        System.out.println("   default-методы дают множественное наследование ПОВЕДЕНИЯ;");
        System.out.println("   поведение может прийти из нескольких источников;");
        System.out.println("   когда источники не сравнимы, язык требует явного выбора —");
        System.out.println("   потому что любое «умолчание» было бы скрытым решением");
        System.out.println("   за программиста, и разные авторы приняли бы разные решения.");
    }
}
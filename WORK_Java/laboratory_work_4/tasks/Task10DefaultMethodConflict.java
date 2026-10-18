package tasks;

/**
 * Задание 10. Конфликт default-методов.
 * Создайте два интерфейса с одинаковым default-методом.
 * Получите ошибку компиляции, затем разрешите конфликт явным override.
 * Объясните, почему множественное наследование поведения требует явного выбора.
 */
public class Task10DefaultMethodConflict {

    interface Auditable {
        default String describe() {
            return "Auditable[запись для аудита]";
        }
    }

    interface Reportable {
        default String describe() {
            return "Reportable[запись для отчёта]";
        }
    }

    /*
     * Если раскомментировать класс ниже без переопределения describe(), возникнет ошибка компиляции:
     * error: class BrokenDocument inherits unrelated defaults for describe() from types Auditable and Reportable
     *
     * static class BrokenDocument implements Auditable, Reportable { }
     */

    static class ResolvedDocument implements Auditable, Reportable {
        private final String title;

        ResolvedDocument(String title) {
            this.title = title;
        }

        @Override
        public String describe() {
            // Явное разрешение конфликта с обращением к конкретным default-реализациям через InterfaceName.super
            return title + " -> " + Auditable.super.describe() + " + " + Reportable.super.describe();
        }
    }

    public static void main(String[] args) {
        ResolvedDocument doc = new ResolvedDocument("Ведомость ЛР4");

        System.out.println("=== 1. Ошибка компиляции до разрешения конфликта ===");
        System.out.println("  javac: class BrokenDocument inherits unrelated defaults for describe()");
        System.out.println("         from types Auditable and Reportable");

        System.out.println("\n=== 2. Результат после явного @Override с вызовом Interface.super.describe() ===");
        System.out.println("  " + doc.describe());

        System.out.println("\n=== 3. Почему множественное наследование поведения требует явного выбора ===");
        System.out.println("  - Интерфейсы Auditable и Reportable не связаны иерархией (ни один не является подтипом другого).");
        System.out.println("  - У компилятора нет детерминированного правила приоритета (порядок в implements не влияет на семантику).");
        System.out.println("  - Неявный выбор зависел бы от порядка перечисления интерфейсов и приводил бы к хрупкому поведению,");
        System.out.println("    поэтому спецификация Java (JLS §9.4.1.3) требует явного переопределения метода в классе.");
    }
}

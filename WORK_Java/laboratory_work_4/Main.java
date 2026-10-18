import tasks.*;

/**
 * Общий лаунчер (вне папки чистых задач tasks/) для последовательного запуска всех 15 заданий.
 */
public class Main {
    public static void main(String[] args) {
        run("Задание 1. Один контракт, две реализации", () -> Task01OneContractTwoImplementations.main(args));
        run("Задание 2. Dynamic dispatch на бумаге", () -> Task02DynamicDispatchOnPaper.main(args));
        run("Задание 3. Полиморфный массив", () -> Task03PolymorphicArray.main(args));
        run("Задание 4. Абстрактный базовый класс", () -> Task04AbstractBaseClass.main(args));
        run("Задание 5. Интерфейс или абстрактный класс", () -> Task05InterfaceOrAbstractClass.main(args));
        run("Задание 6. Переопределение без случайной перегрузки", () -> Task06OverrideWithoutAccidentalOverload.main(args));
        run("Задание 7. Композиция вместо наследования", () -> Task07CompositionOverInheritance.main(args));
        run("Задание 8. Нарушение LSP", () -> Task08LspViolation.main(args));
        run("Задание 9. Политика допуска как стратегия", () -> Task09EnrollmentPolicyStrategy.main(args));
        run("Задание 10. Конфликт default-методов", () -> Task10DefaultMethodConflict.main(args));
        run("Задание 11. Равенство в иерархии", () -> Task11EqualityInHierarchy.main(args));
        run("Задание 12. Sealed-иерархия результата", () -> Task12SealedHierarchyResult.main(args));
        run("Задание 13. Удаление type code", () -> Task13RemoveTypeCode.main(args));
        run("Задание 14. Наследование взрывает комбинации", () -> Task14InheritanceExplosion.main(args));
        run("Задание 15. Расширяемый движок правил", () -> Task15ExtensibleRuleEngine.main(args));
    }

    private static void run(String title, Runnable task) {
        System.out.println("================================================================================");
        System.out.println(">>> " + title);
        System.out.println("================================================================================");
        task.run();
        System.out.println();
    }
}

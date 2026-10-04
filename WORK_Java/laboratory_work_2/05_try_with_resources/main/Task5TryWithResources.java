package ru.sibguti.lab2.task05;

import ru.sibguti.lab2.Console;

/*
 * Задание 5. Try-with-resources (часть 2).
 * Класс Task5TryWithResources показывает три сценария закрытия ресурса:
 * успех, исключение в работе и ошибка одновременно в работе и в close.
 * В третьем сценарии основным считается первое исключение, а исключение
 * из close не теряется - оно попадает в getSuppressed() и печатается
 * как "подавленное". Для сравнения показан ручной try-finally без
 * try-with-resources, где ресурс может остаться незакрытым.
 * Зависимости: работает с Task5LoggableResource из этого же пакета
 * и печатает лог ресурсов, который создаёт сам.
 */
public class Task5TryWithResources {

    public static void main(String[] args) throws Exception {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 5. TRY-WITH-RESOURCES");
        System.out.println();

        System.out.println("--- Сценарий 1: успех, ресурс закрывается по try-with-resources ---");
        useSuccess();

        System.out.println();
        System.out.println("--- Сценарий 2: исключение в работе, ресурс всё равно закрывается ---");
        useFailureInWork();

        System.out.println();
        System.out.println("--- Сценарий 3: ошибка и в работе, и в close, изучаем suppressed ---");
        useFailureInWorkAndClose();

        System.out.println();
        System.out.println("--- Сценарий 4 (对照组): ручной try-finally, close() забыт при throw ---");
        brokenManualVersion();
    }

    private static void useSuccess() {
        try (Task5LoggableResource resource = new Task5LoggableResource("успех", false, false)) {
            resource.use("строка данных");
            System.out.println("   результат: обработка завершилась без ошибок");
        } catch (Exception exception) {
            System.out.println("   блок catch выполнился бы только при ошибке: " + exception.getMessage());
        }
        System.out.println("   порядок: сначала try, потом close, потом catch (если был)");
    }

    private static void useFailureInWork() {
        try (Task5LoggableResource resource = new Task5LoggableResource("сбой в работе", true, false)) {
            resource.use("строка данных");
            System.out.println("   эта строка недостижима: use() бросает исключение");
        } catch (Exception exception) {
            System.out.println("   поймано исключение работы: " + exception.getMessage());
        }
        System.out.println("   close() был вызван до того, как исключение дошло в catch");
    }

    private static void useFailureInWorkAndClose() throws Exception {
        Exception primary = null;
        try (Task5LoggableResource resource = new Task5LoggableResource("двойной сбой", true, true)) {
            resource.use("строка данных");
        } catch (Exception exception) {
            primary = exception;
        }

        System.out.println("   основное исключение : " + primary.getMessage());
        System.out.println("   его тип             : " + primary.getClass().getName());
        Throwable[] suppressed = primary.getSuppressed();
        System.out.println("   подавленных ошибок  : " + suppressed.length);
        for (int i = 0; i < suppressed.length; i++) {
            System.out.println("      suppressed[" + i + "] = " + suppressed[i].getMessage());
        }
        System.out.println("   вывод: try-with-resources закрывает ресурс всегда и не теряет ошибку close");
    }

    private static void brokenManualVersion() {
        Task5LoggableResource resource = new Task5LoggableResource("ручной код", false, false);
        try {
            resource.use("строка данных");
        } catch (Exception exception) {
            System.out.println("   поймано исключение работы: " + exception.getMessage());
        }
        System.out.println("   close() в этом коде вообще не вызывается, ресурс закрыт? " + resource.isClosed());
        System.out.println("   вывод: finally помогает, но его легко забыть или написать не туда");
    }
}

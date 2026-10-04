package ru.sibguti.lab2.task04;

import ru.sibguti.lab2.Console;

/*
 * Задание 4. try-catch-finally.
 * Класс Task4TryCatchFinally содержит шесть коротких сценариев, в которых
 * return или throw стоят внутри try, catch и finally. Для каждого сценария
 * сначала печатается предсказание порядка выполнения, затем запускается сам
 * сценарий и печатается фактический порядок - их можно сравнить.
 * Отдельно показан запрещённый приём return в finally: он не бросает
 * исключение, а молча подменяет результат метода и скрывает саму ошибку.
 * Зависимости: использует Console для UTF-8 вывода и стандартный механизм
 * try-catch-finally языка, других зависимостей нет.
 */
public class Task4TryCatchFinally {

    private static int stepCounter;

    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 4. TRY-CATCH-FINALLY");
        System.out.println();

        System.out.println("--- Сценарий 1: return внутри try, finally без return ---");
        printPrediction("try: шаг 1 -> finally: шаг 2 -> метод вернёт значение из try");
        runReturnFromTry();

        System.out.println("--- Сценарий 2: throw внутри try, исключение поймано в catch ---");
        printPrediction("try: шаг 1 -> catch: шаг 2 -> finally: шаг 3 -> метод вернёт значение из catch");
        runThrowCaughtInCatch();

        System.out.println("--- Сценарий 3: throw внутри try, catch не ловит этот тип ---");
        printPrediction("try: шаг 1 -> finally: шаг 2 -> исключение уходит из метода (catch не выполнился)");
        runThrowNotCaughtDemo();

        System.out.println("--- Сценарий 4: throw внутри finally при return из try ---");
        printPrediction("try: шаг 1 -> finally: шаг 2 и throw -> значение из try потеряно, наружу летит исключение");
        runThrowInFinallyDemo();

        System.out.println("--- Сценарий 5 (ЗАПРЕЩЁННЫЙ ПРИЁМ): return внутри finally ---");
        printPrediction("try: шаг 1 (бросает исключение) -> finally: шаг 2 и return -> "
                + "исключение ПОТЕРЯНО, метод вернёт значение из finally");
        runBrokenReturnInFinally();

        System.out.println();
        System.out.println("Вывод: finally выполняется всегда, но возвращать из него значение нельзя.");
        System.out.println("return в finally завершает метод раньше, чем JVM создаст объект исключения,");
        System.out.println("поэтому вызывающий видит обычный результат и не знает, что произошла ошибка.");
    }

    /*
     * Метод runThrowNotCaughtDemo запускает сценарий 3 и ловит исключение,
     * которое метод отправил вверх по стеку. Так видно, что finally выполнился,
     * catch так и не выполнился, а вызывающий всё равно получил ошибку.
     * Его вызывает main.
     */
    private static void runThrowNotCaughtDemo() {
        try {
            runThrowNotCaught();
        } catch (NumberFormatException exception) {
            log("main поймал исключение, ушедшее из метода: " + exception.getMessage());
        }
    }

    /*
     * Метод runThrowInFinallyDemo запускает сценарий 4 и ловит исключение
     * из finally. Его вызывает main; он показывает, что значение, собранное
     * в try, до вызывающего не дошло вообще.
     */
    private static void runThrowInFinallyDemo() {
        try {
            runThrowInFinally();
        } catch (IllegalStateException exception) {
            log("main поймал исключение из finally: " + exception.getMessage());
        }
    }

    /*
     * Метод printPrediction печатает ожидаемый порядок выполнения для сценария.
     * Его вызывает main перед каждым запуском сценария, чтобы ученик сравнил
     * предсказание с фактическим выводом и не заучивал результат вслепую.
     */
    private static void printPrediction(String prediction) {
        System.out.println("Предсказание: " + prediction);
    }

    /*
     * Метод log печатает один шаг сценария с номером, чтобы порядок
     * выполнения было видно буквально. Его вызывают все шесть сценариев,
     * а номер он берёт из общего счётчика stepCounter.
     */
    private static void log(String message) {
        stepCounter = stepCounter + 1;
        System.out.println("   шаг " + stepCounter + ": " + message);
    }

    /*
     * Сценарий 1: return стоит в try, finally ничего не возвращает.
     * Сначала выполняется try, запоминается значение возврата, затем
     * всегда выполняется finally, и только после этого метод возвращает
     * значение, сохранённое в try. Его вызывает main.
     */
    private static String runReturnFromTry() {
        stepCounter = 0;
        try {
            log("try: собираем результат");
            return "значение из try";
        } catch (RuntimeException exception) {
            log("catch: не выполняется, исключения не было");
            return "значение из catch";
        } finally {
            log("finally: выполняется всегда, но return здесь запрещён");
        }
    }

    /*
     * Сценарий 2: try бросает исключение, catch его ловит и возвращает значение.
     * finally выполняется между catch и возвратом, поэтому finally не может
     * изменить уже выбранное значение, если не вернёт своё (что запрещено).
     * Его вызывает main.
     */
    private static String runThrowCaughtInCatch() {
        stepCounter = 0;
        try {
            log("try: бросаем исключение");
            throw new IllegalStateException("сбой в try");
        } catch (RuntimeException exception) {
            log("catch: исключение поймано, " + exception.getMessage());
            return "значение из catch";
        } finally {
            log("finally: выполняется до фактического выхода из метода");
        }
    }

    /*
     * Сценарий 3: try бросает исключение, но catch ловит только другой тип,
     * поэтому catch не выполняется вовсе. finally всё равно выполняется,
     * а исключение продолжает свой путь вверх по стеку вызовов.
     * Его вызывает main.
     */
    private static String runThrowNotCaught() {
        stepCounter = 0;
        try {
            log("try: бросаем NumberFormatException");
            throw new NumberFormatException("не число");
        } catch (IllegalStateException exception) {
            log("catch: не выполняется, тип не тот");
            return "значение из catch";
        } finally {
            log("finally: выполняется, потом исключение уходит наружу");
        }
    }

    /*
     * Сценарий 4: finally бросает новое исключение. Значение, которое
     * собирал try, теряется, потому что метод не может вернуть его
     * одновременно с исключением: наружу уходит ошибка из finally.
     * Его вызывает main.
     */
    private static String runThrowInFinally() {
        stepCounter = 0;
        try {
            log("try: результат собран, но он уже не будет возвращён");
            return "значение из try";
        } catch (RuntimeException exception) {
            log("catch: не выполняется");
            return "значение из catch";
        } finally {
            log("finally: бросаем новое исключение вместо возврата");
            throw new IllegalStateException("сбой при закрытии ресурса в finally");
        }
    }

    /*
     * Сценарий 5: запрещённый приём - return в finally. Метод собран так,
     * что try всегда падает, catch это исключение ловит и заменяет своим
     * значением, но finally всё равно выполняется и тоже возвращает значение.
     * Из-за этого try никогда не доходит до return, а исходная ошибка теряется:
     * наружу метод отдаёт "значение из finally" и не бросает ничего.
     * Именно так return в finally способен скрыть исключение.
     * Его вызывает main как запрещённый пример.
     */
    private static String runBrokenReturnInFinally() {
        stepCounter = 0;
        try {
            log("try: бросаем исключение");
            throw new IllegalArgumentException("настоящая ошибка");
        } catch (RuntimeException exception) {
            log("catch: поймали настоящую ошибку, но вернуть её нельзя");
            return "значение из catch";
        } finally {
            log("finally: return здесь СКРЫВАЕТ исключение");
            return "значение из finally";
        }
    }
}

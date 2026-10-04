package ru.sibguti.lab2.task01;

import ru.sibguti.lab2.Console;

/*
 * Задание 1. Чтение stack trace.
 * Класс Task1StackTrace строит цепочку вызовов из четырёх методов, в самом конце
 * которой происходит ошибка разбора числа, а затем разбирает получившийся
 * stack trace по четырём пунктам: тип исключения, сообщение, место возникновения
 * и полный путь вызовов. В конце показано, где именно нужно исправлять причину:
 * не в последней строке трассировки, а на границе, где в домен попадают
 * внешние данные с "грязными" символами.
 * Зависимости: использует Console только для включения UTF-8 вывода,
 * остальной код работает на стандартных классах Java (RuntimeException,
 * StackTraceElement) и не зависит ни от какого своего пакета.
 */
public class Task1StackTrace {

    private static final String BROKEN_INPUT = " 19 лет ";

    /*
     * Главный метод запускает демонстрацию задания: сначала строит цепочку
     * из четырёх методов и получает исключение, потом разбирает stack trace,
     * потом выполняет исправленную цепочку на тех же входных данных.
     * Его вызывает среда разработки (Run/Debug), внутри себя он вызывает
     * enrollStudentBroken, printExceptionInfo и enrollStudentFixed.
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 1. ЧТЕНИЕ STACK TRACE");
        System.out.println();

        System.out.println("Шаг 1. Получаем исключение из цепочки четырёх методов.");
        try {
            enrollStudentBroken(BROKEN_INPUT);
        } catch (RuntimeException exception) {
            System.out.println();
            System.out.println("Шаг 2. Читаем stack trace по пунктам.");
            printExceptionInfo(exception);
            System.out.println();
            System.out.println("Оригинальная трассировка:");
            exception.printStackTrace(System.out);
        }

        System.out.println();
        System.out.println("Шаг 3. Исправляем причину, а не последнюю строку трассировки.");
        enrollStudentFixed(BROKEN_INPUT);
        enrollStudentFixed(" 20 ");
        showFixedRejection("не число");
    }

    /*
     * Метод showFixedRejection вызывает исправленную цепочку с заведомо
     * негодными данными и ловит её исключение, чтобы демонстрация не
     * оборвалась. Его вызывает main в конце шага 3.
     */
    private static void showFixedRejection(String rawAge) {
        try {
            enrollStudentFixed(rawAge);
        } catch (IllegalArgumentException exception) {
            System.out.println("   Отказ на границе с понятным сообщением: " + exception.getMessage());
            System.out.println("   вместо NumberFormatException из глубины Integer.parseInt");
        }
    }

    /*
     * Метод printExceptionInfo печатает разобранную трассировку по четырём
     * пунктам задания: тип исключения, его сообщение, место возникновения
     * и весь путь вызовов снизу вверх. Его вызывает main, чтобы показать,
     * что трассировка читается как обычный текст, а не «магический» лог.
     * Внутри он опирается на findOrigin и describeFrame.
     */
    public static void printExceptionInfo(RuntimeException exception) {
        System.out.println("1) Тип исключения            : " + exception.getClass().getName());
        System.out.println("2) Сообщение                 : " + exception.getMessage());
        StackTraceElement origin = findOrigin(exception);
        System.out.println("3) Место возникновения       : " + describeFrame(origin));
        System.out.println("4) Путь вызовов (снизу вверх):");
        StackTraceElement[] frames = exception.getStackTrace();
        for (int i = frames.length - 1; i >= 0; i--) {
            System.out.println("     " + (frames.length - i) + ") " + describeFrame(frames[i]));
        }
    }

    /*
     * Метод findOrigin возвращает первый кадр трассировки, то есть строку,
     * где исключение было создано и выброшено, - именно это и есть
     * "место возникновения". Его вызывает printExceptionInfo.
     * Если трассировка пуста (исключение собрано вручную), возвращается null,
     * чтобы вызывающий код не получил NullPointerException.
     */
    public static StackTraceElement findOrigin(RuntimeException exception) {
        StackTraceElement[] frames = exception.getStackTrace();
        if (frames.length == 0) {
            return null;
        }
        return frames[0];
    }

    /*
     * Метод describeFrame превращает один кадр трассировки в читаемую строку
     * вида класс.метод (файл:строка). Его вызывают printExceptionInfo и
     * findOrigin-печать, чтобы формат вывода был одинаковым везде.
     */
    private static String describeFrame(StackTraceElement frame) {
        if (frame == null) {
            return "(кадр отсутствует: трассировка была создана вручную)";
        }
        return frame.getClassName() + "." + frame.getMethodName()
                + " (" + frame.getFileName() + ":" + frame.getLineNumber() + ")";
    }

    /*
     * Метод enrollStudentBroken - первый метод сломанной цепочки (кадр 1).
     * Он вызывается из main и передаёт данные дальше, ничего не проверяя,
     * поэтому в нём нет ни одного catch: так видно, что try/catch в main
     * не является исправлением ошибки, а лишь hides её от пользователя.
     */
    public static void enrollStudentBroken(String rawAge) {
        StudentRecord record = createStudentBroken(rawAge);
        System.out.println("Зачислен студент: " + record);
    }

    /*
     * Метод createStudentBroken - кадр 2 сломанной цепочки: он превращает
     * строку в возраст и создаёт учётную запись. Его вызывает enrollStudentBroken,
     * а сам он вызывает parseAgeBroken. Ошибка возникает глубже, чем этот кадр,
     * поэтому строка "место возникновения" в трассировке указывает глубже.
     */
    private static StudentRecord createStudentBroken(String rawAge) {
        int age = parseAgeBroken(rawAge);
        return new StudentRecord("S-01", age);
    }

    /*
     * Метод parseAgeBroken - кадр 3 сломанной цепочки, который добавляет
     * к вызову доменный смысл ("возраст"), но сам ничего не проверяет.
     * Его вызывает createStudentBroken, а он вызывает parseNumberBroken.
     */
    private static int parseAgeBroken(String rawAge) {
        return parseNumberBroken(rawAge);
    }

    /*
     * Метод parseNumberBroken - кадр 4 и самое глубокое место сломанной цепочки.
     * Здесь Integer.parseInt получает строку с пробелами и буквами и выбрасывает
     * NumberFormatException (наследник RuntimeException, то есть unchecked).
     * Его вызывает parseAgeBroken; исправлять ошибку нужно входные данные,
     * а не этот метод.
     */
    private static int parseNumberBroken(String rawAge) {
        System.out.println("   Вызов: parseNumberBroken(\"" + rawAge + "\")");
        return Integer.parseInt(rawAge);
    }

    /*
     * Метод enrollStudentFixed - первый метод исправленной цепочки (кадр 1).
     * Он вызывается из main уже после того, как причина ошибки устранена,
     * и выполняет ту же работу, что и сломанная версия, но без исключения
     * на нормализованных данных и с понятным сообщением на негодных.
     */
    public static void enrollStudentFixed(String rawAge) {
        StudentRecord record = createStudentFixed(rawAge);
        System.out.println("Зачислен студент: " + record);
    }

    /*
     * Метод createStudentFixed - кадр 2 исправленной цепочки, его вызывает
     * enrollStudentFixed, а сам он вызывает parseAgeFixed. Отличие от
     * сломанной версии только в том, что данные дошли уже очищенными.
     */
    private static StudentRecord createStudentFixed(String rawAge) {
        int age = parseAgeFixed(rawAge);
        return new StudentRecord("S-01", age);
    }

    /*
     * Метод parseAgeFixed - кадр 3 исправленной цепочки, его вызывает
     * createStudentFixed, а он вызывает parseNumberFixed.
     */
    private static int parseAgeFixed(String rawAge) {
        return parseNumberFixed(rawAge);
    }

    /*
     * Метод parseNumberFixed - кадр 4, здесь сделано настоящее исправление
     * причины: строка очищается от пробелов и суффиксов (" 19 лет " -> "19"),
     * и только потом проверяется, что остались цифры. Если данных нет,
     * выбрасывается IllegalArgumentException с понятным сообщением вместо
     * NumberFormatException из глубины библиотеки.
     * Его вызывает parseAgeFixed, а проверить его поведение удобно в main.
     */
    private static int parseNumberFixed(String rawAge) {
        String cleaned = rawAge == null ? "" : rawAge.trim();
        int spaceIndex = cleaned.indexOf(' ');
        if (spaceIndex >= 0) {
            cleaned = cleaned.substring(0, spaceIndex);
        }
        System.out.println("   Вызов: parseNumberFixed(\"" + rawAge + "\") -> очищено \"" + cleaned + "\"");
        if (!isDigits(cleaned)) {
            throw new IllegalArgumentException("возраст должен состоять только из цифр, получено: \"" + rawAge + "\"");
        }
        return Integer.parseInt(cleaned);
    }

    /*
     * Метод isDigits проверяет, что строка состоит только из цифр и не пуста.
     * Его вызывает parseNumberFixed как простую валидацию перед разбором,
     * чтобы не получать NumberFormatException из библиотеки.
     */
    private static boolean isDigits(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /*
     * Вложенный класс StudentRecord - простой объект учётной записи студента,
     * он нужен только для того, чтобы показать успешный результат исправленной
     * цепочки методов. Создают его методы createStudentBroken и createStudentFixed,
     * а читает его toString при печати.
     */
    private static final class StudentRecord {

        private final String id;
        private final int age;

        private StudentRecord(String id, int age) {
            this.id = id;
            this.age = age;
        }

        public String toString() {
            return id + ", возраст " + age;
        }
    }
}

package ru.sibguti.lab2.task02;

import ru.sibguti.lab2.Console;

/*
 * Задание 2. Checked или unchecked.
 * Класс Task2CheckedUnchecked содержит таблицу из десяти рабочих ситуаций и
 * для каждой ситуации хранит выбранное решение: checked exception,
 * unchecked exception, другой результат или невозможность восстановления.
 * Кроме самого ответа в ситуации записано объяснение, почему выбран именно
 * такой вариант - это и есть главная часть задания.
 * Зависимости: использует Console для UTF-8 вывода, свои собственные
 * вложенные типы SituationType и Situation, и ничего больше, поэтому файл
 * можно запускать независимо от остальных заданий.
 */
public class Task2CheckedUnchecked {

    /*
     * Перечисление SituationType содержит четыре допустимых ответа задания:
     * CHECKED - внешняя причина, вызывающий обязан обработать (throws в сигнатуре),
     * UNCHECKED - нарушение контракта внутри программы, компилятор не заставляет,
     * OTHER_RESULT - отказ является нормальным исходом и возвращается значением
     * (Optional, типизированный result или код ошибки), а не исключением,
     * UNRECOVERABLE - состояние процесса настолько плохо, что продолжать
     * работу нельзя (OutOfMemoryError, StackOverflowError).
     */
    public enum SituationType {
        CHECKED("checked exception"),
        UNCHECKED("unchecked exception"),
        OTHER_RESULT("другой результат"),
        UNRECOVERABLE("невозможность восстановления");

        private final String title;

        SituationType(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    /*
     * Класс Situation - это одна строка таблицы: номер, название ситуации,
     * выбранный тип ответа, пример типа исключения и текст объяснения.
     * Его создаёт метод fillSituations, а читает таблицу метод printTable.
     */
    public static class Situation {

        private final int number;
        private final String title;
        private final SituationType type;
        private final String exceptionExample;
        private final String explanation;

        public Situation(int number, String title, SituationType type, String exceptionExample, String explanation) {
            this.number = number;
            this.title = title;
            this.type = type;
            this.exceptionExample = exceptionExample;
            this.explanation = explanation;
        }

        public int getNumber() {
            return number;
        }

        public String getTitle() {
            return title;
        }

        public SituationType getType() {
            return type;
        }

        public String getExceptionExample() {
            return exceptionExample;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    /*
     * Главный метод распечатывает заголовок, саму таблицу из десяти ситуаций
     * и итог: сколько ситуаций попало в каждую из четырёх категорий.
     * Его вызывает среда разработки, внутри он вызывает printTable и printSummary.
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 2. CHECKED ИЛИ UNCHECKED");
        System.out.println();
        Situation[] situations = fillSituations();
        printTable(situations);
        System.out.println();
        printSummary(situations);
    }

    /*
     * Метод fillSituations собирает все десять ситуаций задания и возвращает
     * их массивом. Каждая ситуация - это конкретный пример из лабораторной
     * работы CourseHub. Его вызывает main, больше его никто не вызывает:
     * данные таблицы неизменяемы, поэтому готовить их каждый раз не нужно.
     */
    public static Situation[] fillSituations() {
        Situation[] situations = new Situation[10];

        situations[0] = new Situation(1, "Неверный аргумент метода",
                SituationType.UNCHECKED, "IllegalArgumentException",
                "Аргумент создаёт сам программист, снаружи его не поступает: "
                        + "исправляется в коде вызова, а catch только прячет ошибку.");

        situations[1] = new Situation(2, "Нарушение состояния объекта",
                SituationType.UNCHECKED, "IllegalStateException",
                "Объект уже в неверном состоянии (курс завершён, а его снова закрывают). "
                        + "Это ошибка порядка вызовов, а не внешних данных.");

        situations[2] = new Situation(3, "Отсутствующий файл",
                SituationType.CHECKED, "FileNotFoundException",
                "Причина вне программы, вызывающий может решить: создать файл, "
                        + "сообщить пользователю или отказать. Компилятор заставляет решить это явно.");

        situations[3] = new Situation(4, "Сетевой отказ (таймаут, обрыв)",
                SituationType.CHECKED, "IOException",
                "Внешняя среда, отказ временный: повтор попытки или отказ клиенту. "
                        + "Подходит и Result-объект, если метод обязан вернуть ответ.");

        situations[4] = new Situation(5, "Ошибка конфигурации (не задан адрес сервиса)",
                SituationType.OTHER_RESULT, "ErrorCode.SERVICE_UNAVAILABLE",
                "Чинить нужно конфигурацию или развёртывание, а не метод. "
                        + "Публичный слой отдаёт стабильный код результата, а не бросает исключение клиенту.");

        situations[5] = new Situation(6, "Недостаток памяти (OutOfMemoryError)",
                SituationType.UNRECOVERABLE, "OutOfMemoryError",
                "Виртуальная машина не может продолжать работу, объект для обработки "
                        + "обычно уже не существует. Ловить нечего: нужен разбор дампа и разгрузка.");

        situations[6] = new Situation(7, "Переполнение диска при записи отчёта",
                SituationType.CHECKED, "IOException",
                "Внешняя причина, которую можно устранить и повторить запись. "
                        + "Но если отчёт не обязателен, разумнее отдать усечённый результат.");

        situations[7] = new Situation(8, "Нет свободных мест при зачислении",
                SituationType.OTHER_RESULT, "ErrorCode.NO_FREE_SEATS",
                "Бизнес-правило, а не поломка: исход «отказ» заранее предусмотрен сценарием "
                        + "и возвращается кодом ошибки без исключения.");

        situations[8] = new Situation(9, "Повторная заявка студента",
                SituationType.OTHER_RESULT, "ErrorCode.ALREADY_ENROLLED",
                "Ожидаемый отказ, для которого есть точный код. Исключение здесь только "
                        + "заставит писать одинаковый catch во всех вызывающих методах.");

        situations[9] = new Situation(10, "StackOverflowError (зацикленная рекурсия)",
                SituationType.UNRECOVERABLE, "StackOverflowError",
                "Стек потока исчерпан, выполнение нельзя продолжить в этом потоке. "
                        + "Любая обработка скроет симптом, но не вернёт управление.");

        return situations;
    }

    /*
     * Метод printTable печатает все ситуации в удобном виде: номер, название,
     * выбранный ответ, пример типа и объяснение. Его вызывает main.
     * Он ничего не меняет, только печатает, поэтому безопасный для повторного запуска.
     */
    public static void printTable(Situation[] situations) {
        for (int i = 0; i < situations.length; i++) {
            Situation situation = situations[i];
            System.out.println(situation.getNumber() + ") " + situation.getTitle());
            System.out.println("   Ответ: " + situation.getType().name()
                    + " (" + situation.getType().getTitle() + ")");
            System.out.println("   Пример: " + situation.getExceptionExample());
            System.out.println("   Почему: " + situation.getExplanation());
            System.out.println();
        }
    }

    /*
     * Метод printSummary считает, сколько ситуаций попало в каждую категорию,
     * и печатает эти четыре числа. Его вызывает main в конце таблицы,
     * чтобы было видно, что использованы все четыре допустимых ответа.
     */
    public static void printSummary(Situation[] situations) {
        SituationType[] types = SituationType.values();
        int[] counters = new int[types.length];
        for (int i = 0; i < situations.length; i++) {
            for (int t = 0; t < types.length; t++) {
                if (situations[i].getType() == types[t]) {
                    counters[t] = counters[t] + 1;
                }
            }
        }
        System.out.println("ИТОГО ПО КАТЕГОРИЯМ:");
        for (int t = 0; t < types.length; t++) {
            System.out.println("   " + types[t].name() + " (" + types[t].getTitle() + "): " + counters[t]);
        }
    }
}

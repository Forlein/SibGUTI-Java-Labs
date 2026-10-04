package ru.sibguti.lab2.task06;

import ru.sibguti.lab2.Console;

import java.util.ArrayList;
import java.util.List;

/*
 * Задание 6. Граница перевода исключения (часть 3).
 * Класс Task6ImportService - уровень импорта. Он ловит ровно одно исключение -
 * NumberFormatException из парсера - и превращает его в CourseImportException,
 * добавляя номер записи, имя поля и исходную причину в cause.
 * Ловить Exception здесь нельзя: вместе с NumberFormatException туда попали бы
 * и совсем не связанные ошибки, которые потеряли бы своё имя и контекст.
 * Его вызывает main, а тесты задания 11 проверяют его поля и неизменность
 * уже импортированных записей после отказа.
 */
public class Task6ImportService {

    private final Task6CourseParser parser;
    private final List<Task6CourseParser.CourseRecord> imported;

    public Task6ImportService() {
        this(new Task6CourseParser());
    }

    public Task6ImportService(Task6CourseParser parser) {
        this.parser = parser;
        this.imported = new ArrayList<Task6CourseParser.CourseRecord>();
    }

    public List<Task6CourseParser.CourseRecord> importAll(List<String> lines) throws CourseImportException {
        for (int index = 0; index < lines.size(); index++) {
            int recordNumber = index + 1;
            String line = lines.get(index);
            try {
                imported.add(parser.parse(line));
            } catch (NumberFormatException exception) {
                throw new CourseImportException(recordNumber, "hours",
                        "значение не является целым числом", exception);
            }
        }
        return getImported();
    }

    public List<Task6CourseParser.CourseRecord> getImported() {
        return imported;
    }

    /*
     * Главный метод показывает три случая: полностью корректный файл,
     * файл с одной плохой записью и файл с нарушенной структурой строки.
     * Его вызывает среда разработки, внутри он вызывает importAll.
     */
    public static void main(String[] args) {
        Console.enableUtf8();
        System.out.println("ЗАДАНИЕ 6. ГРАНИЦА ПЕРЕВОДА ИСКЛЮЧЕНИЯ");
        System.out.println();

        List<String> goodLines = new ArrayList<String>();
        goodLines.add("CS101;Основы Java;72");
        goodLines.add("CS102;Базы данных;80");

        Task6ImportService service = new Task6ImportService();
        try {
            List<Task6CourseParser.CourseRecord> records = service.importAll(goodLines);
            System.out.println("Сценарий 1: файл корректен, импортировано записей = " + records.size());
        } catch (CourseImportException exception) {
            System.out.println("Сценарий 1: неожиданный отказ: " + exception.getMessage());
        }

        System.out.println();
        List<String> brokenLines = new ArrayList<String>();
        brokenLines.add("CS103;Алгоритмы;64");
        brokenLines.add("CS104;Компиляторы;не_число");

        Task6ImportService second = new Task6ImportService();
        try {
            second.importAll(brokenLines);
        } catch (CourseImportException exception) {
            System.out.println("Сценарий 2: NumberFormatException переведён на границе импорта");
            System.out.println("   тип исключения  : " + exception.getClass().getName());
            System.out.println("   message         : " + exception.getMessage());
            System.out.println("   recordNumber    : " + exception.getRecordNumber());
            System.out.println("   fieldName       : " + exception.getFieldName());
            System.out.println("   cause (тип)     : " + exception.getCause().getClass().getName());
            System.out.println("   cause (сообщение): " + exception.getCause().getMessage());
            System.out.println("   причина сохранена, контекст записи не потерян");
        }

        System.out.println();
        List<String> malformedLines = new ArrayList<String>();
        malformedLines.add("CS105;Сети;32;лишнее поле");
        Task6ImportService third = new Task6ImportService();
        try {
            third.importAll(malformedLines);
        } catch (CourseImportException exception) {
            System.out.println("Сценарий 3: поймана ошибка перевода: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.out.println("Сценарий 3: ошибка структуры строки НЕ скрыта импортом, потому что ловится");
            System.out.println("   только NumberFormatException. Текст: " + exception.getMessage());
        }
    }
}

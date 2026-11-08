/*
Задание 6. Граница перевода исключения
Низкоуровневый parser выбрасывает `NumberFormatException`. 
На уровне импорта преобразуйте её в `CourseImportException` 
с номером записи и cause. Не ловите `Exception` и не теряйте 
контекст.
*/
public class Main {

    static class CourseImportException extends Exception {
        final int recordNumber;
        CourseImportException(int recordNumber, Throwable cause) {
            super("Import failed at record " + recordNumber, cause);
            this.recordNumber = recordNumber;
        }
    }

    // НИЗКИЙ уровень: парсер, ничего не знает про "записи"
    static int parse(String s) {
        return Integer.parseInt(s);   // "abc" → NumberFormatException
    }

    // ГРАНИЦА: импорт, добавляет номер записи и cause
    static int importRecord(int recordNumber, String s) throws CourseImportException {
        try {
            return parse(s);
        } catch (NumberFormatException e) {                       // ловим КОНКРЕТНЫЙ тип
            throw new CourseImportException(recordNumber, e);     // cause сохранён
        }
    }

    public static void main(String[] args) {
        String[] records = {"10", "abc", "30"};
        for (int i = 0; i < records.length; i++) {
            try {
                System.out.println("record " + i + " = " + importRecord(i, records[i]));
            } catch (CourseImportException e) {
                System.out.println("error: " + e.getMessage());
                System.out.println("cause: " + e.getCause());
                System.out.println("recordNumber: " + e.recordNumber);
            }
        }
    }
}

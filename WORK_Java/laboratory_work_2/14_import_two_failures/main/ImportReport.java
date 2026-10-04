package ru.sibguti.lab2.task14;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 14 без изменений.
 * Зачем он нужен в этом задании: отчёт об успешном импорте: сколько записей опубликовано.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Задание 14. Ресурс с двумя отказами (часть 2).
 * Класс ImportReport - успешный результат импорта: сколько записей разобрано
 * и опубликовано. Отчёт возвращается только тогда, когда все записи разобраны,
 * а хранилище приняло их целиком, поэтому непустой отчёт всегда означает
 * полностью опубликованный результат.
 * Зависимости: его создаёт и возвращает CourseImporter, читает публичный
 * слой CourseHubFacade (задание 15) и тесты заданий 14 и 15.
 */
public class ImportReport {

    private final int recordsImported;

    public ImportReport(int recordsImported) {
        this.recordsImported = recordsImported;
    }

    public int getRecordsImported() {
        return recordsImported;
    }

    public String toString() {
        return "ImportReport[imported=" + recordsImported + "]";
    }
}

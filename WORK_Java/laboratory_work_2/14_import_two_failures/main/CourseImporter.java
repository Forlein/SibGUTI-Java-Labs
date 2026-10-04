package ru.sibguti.lab2.task14;

import ru.sibguti.lab2.domain.Course;
import ru.sibguti.lab2.domain.CourseCode;
import ru.sibguti.lab2.domain.ErrorCode;
import ru.sibguti.lab2.hub.CourseRepository;
import ru.sibguti.lab2.hub.RepositoryUnavailableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 14, перенесён в задание 14 без изменений.
 * Зачем он нужен в этом задании: читает записи курсов и публикует их в хранилище целиком.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Задание 14. Ресурс с двумя отказами (часть 3).
 * Класс CourseImporter читает записи курсов из абстрактного Reader
 * в формате "код;название;часы;вместимость;prerequisite" и публикует их
 * в хранилище только целиком: сначала все записи собираются в список,
 * и лишь после успешного чтения вызывается repository.saveAll.
 * Благодаря этому частично опубликованного результата не бывает.
 * Ресурс закрывается через try-with-resources, поэтому при отказе разбора
 * ошибка закрытия не теряется, а попадает в getSuppressed() основного
 * исключения. Ловит импортёр только два типа: IOException от источника
 * и RepositoryUnavailableException от хранилища.
 * Зависимости: использует доменные Course и CourseCode, хранилище CourseRepository
 * и свои ImportFailedException с ImportReport; вызывает его публичный слой
 * CourseHubFacade (задание 15) и тесты заданий 14 и 15.
 */
public class CourseImporter {

    private static final String FIELD_SEPARATOR = ";";
    private static final int FIELDS_COUNT = 5;
    private static final String NO_PREREQUISITE = "-";

    private final CourseRepository repository;

    public CourseImporter(CourseRepository repository) {
        this.repository = repository;
    }

    public ImportReport importFrom(Reader reader) throws ImportFailedException {
        List<Course> parsed = new ArrayList<Course>();
        try (Reader source = reader) {
            BufferedReader lines = new BufferedReader(source);
            String line = lines.readLine();
            while (line != null) {
                if (!isBlank(line)) {
                    int recordNumber = parsed.size() + 1;
                    parsed.add(parseRecord(line.trim(), recordNumber, parsed.size()));
                }
                line = lines.readLine();
            }
        } catch (IOException exception) {
            throw new ImportFailedException(ErrorCode.IMPORT_SOURCE_UNREADABLE,
                    -1, parsed.size(), exception);
        }

        try {
            repository.saveAll(parsed);
        } catch (RepositoryUnavailableException exception) {
            throw new ImportFailedException(ErrorCode.SERVICE_UNAVAILABLE,
                    -1, parsed.size(), exception);
        }
        return new ImportReport(parsed.size());
    }

    private Course parseRecord(String line, int recordNumber, int recordsProcessed)
            throws ImportFailedException {
        String[] fields = line.split(FIELD_SEPARATOR);
        if (fields.length != FIELDS_COUNT) {
            throw new ImportFailedException(ErrorCode.MALFORMED_RECORD, recordNumber, recordsProcessed,
                    new IllegalArgumentException("ожидалось " + FIELDS_COUNT + " полей"));
        }

        int hours = parseHours(fields[2], recordNumber, recordsProcessed);
        int capacity = parseCapacity(fields[3], recordNumber, recordsProcessed);
        String prerequisite = fields[4].trim();
        if (prerequisite.isEmpty() || NO_PREREQUISITE.equals(prerequisite)) {
            prerequisite = null;
        }

        try {
            return new Course(fields[0], fields[1], hours, capacity, prerequisite);
        } catch (IllegalArgumentException exception) {
            throw new ImportFailedException(ErrorCode.MALFORMED_RECORD, recordNumber, recordsProcessed, exception);
        }
    }

    private int parseHours(String rawHours, int recordNumber, int recordsProcessed)
            throws ImportFailedException {
        try {
            return Integer.parseInt(rawHours.trim());
        } catch (NumberFormatException exception) {
            throw new ImportFailedException(ErrorCode.MALFORMED_RECORD, recordNumber, recordsProcessed, exception);
        }
    }

    private int parseCapacity(String rawCapacity, int recordNumber, int recordsProcessed)
            throws ImportFailedException {
        try {
            return Integer.parseInt(rawCapacity.trim());
        } catch (NumberFormatException exception) {
            throw new ImportFailedException(ErrorCode.MALFORMED_RECORD, recordNumber, recordsProcessed, exception);
        }
    }

    /*
     * Метод isBlank сообщает, состоит ли строка только из пробелов.
     * Его вызывает importFrom, чтобы молча пропускать пустые строки файла,
     * не считая их записями и не сдвигая номера оставшихся записей.
     */
    private boolean isBlank(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

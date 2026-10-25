import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final int MAX_STORED_ERRORS = 5;
    private static final int CANCEL_AFTER_ERRORS = 10;

    public static void main(String[] args) {
        Path smallFile = null;
        Path largeFile = null;
        boolean ok = true;

        try {
            Path dir = Files.createTempDirectory("stream-import-");
            smallFile = dir.resolve("small.csv");
            largeFile = dir.resolve("large.csv");

            writeSample(smallFile, 5_000, 1_000);
            writeSample(largeFile, 50_000, 1_000);

            System.out.println("--- Небольшой файл: ошибок меньше порога ---");
            ImportSummary completed = importCatalog(smallFile, MAX_STORED_ERRORS, CANCEL_AFTER_ERRORS);
            printSummary(completed);
            if (completed.cancelled()) {
                System.out.println("ОШИБКА: импорт не должен был отменяться");
                ok = false;
            }
            if (completed.totalErrors() != 5 || completed.sampleErrors().size() != 5) {
                System.out.println("ОШИБКА: неверные счётчики ошибок");
                ok = false;
            }

            System.out.println("--- Большой файл: отмена по порогу ---");
            ImportSummary cancelled = importCatalog(largeFile, MAX_STORED_ERRORS, CANCEL_AFTER_ERRORS);
            printSummary(cancelled);
            if (!cancelled.cancelled()) {
                System.out.println("ОШИБКА: ожидалась отмена после порога");
                ok = false;
            }
            if (cancelled.totalErrors() != CANCEL_AFTER_ERRORS) {
                System.out.println("ОШИБКА: порог отмены не совпал");
                ok = false;
            }
            if (cancelled.sampleErrors().size() != MAX_STORED_ERRORS) {
                System.out.println("ОШИБКА: список ошибок не ограничен");
                ok = false;
            }

            System.out.println("Память: файл читается построчно, хранится не более "
                    + MAX_STORED_ERRORS + " ошибок, валидные записи не накапливаются.");
        } catch (IOException e) {
            System.err.println("Ошибка потокового импорта: " + e.getMessage());
            ok = false;
        } finally {
            deleteQuietly(smallFile);
            deleteQuietly(largeFile);
            if (smallFile != null) {
                deleteQuietly(smallFile.getParent());
            }
        }

        if (!ok) {
            System.exit(1);
        }
    }

    private static ImportSummary importCatalog(Path path, int maxStoredErrors, int cancelAfterErrors)
            throws IOException {
        if (maxStoredErrors < 0) {
            throw new IllegalArgumentException("maxStoredErrors не может быть отрицательным");
        }
        if (cancelAfterErrors <= 0) {
            throw new IllegalArgumentException("cancelAfterErrors должен быть положительным");
        }

        long totalLines = 0;
        long validRecords = 0;
        BoundedErrorCollector collector = new BoundedErrorCollector(maxStoredErrors);
        boolean cancelled = false;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;

                String[] parts = line.split(";", -1);
                if (parts.length != 2) {
                    collector.add(new ImportError(totalLines, "ожидается 2 поля"));
                } else {
                    String name = parts[0].trim();
                    String hoursText = parts[1].trim();

                    if (name.isEmpty()) {
                        collector.add(new ImportError(totalLines, "пустое название"));
                    } else {
                        try {
                            int hours = Integer.parseInt(hoursText);
                            if (hours <= 0) {
                                collector.add(new ImportError(totalLines, "часы должны быть положительными"));
                            } else {
                                validRecords++;
                            }
                        } catch (NumberFormatException e) {
                            collector.add(new ImportError(totalLines, "неверное число часов: " + hoursText));
                        }
                    }
                }

                if (collector.totalErrors() >= cancelAfterErrors) {
                    cancelled = true;
                    break;
                }
            }
        }

        return new ImportSummary(totalLines, validRecords, collector.totalErrors(),
                collector.storedErrors(), cancelled);
    }

    private static void writeSample(Path path, int totalLines, int errorEvery) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (int i = 1; i <= totalLines; i++) {
                if (i % errorEvery == 0) {
                    writer.write("Курс " + i + ";ошибка");
                } else {
                    writer.write("Курс " + i + ";" + (36 + (i % 5) * 12));
                }
                writer.newLine();
            }
        }
    }

    private static void printSummary(ImportSummary summary) {
        System.out.println("Обработано строк: " + summary.totalLines());
        System.out.println("Корректных записей: " + summary.validRecords());
        System.out.println("Всего ошибок: " + summary.totalErrors());
        System.out.println("Сохранено ошибок: " + summary.sampleErrors().size());
        for (ImportError error : summary.sampleErrors()) {
            System.out.println("  строка " + error.lineNumber() + ": " + error.message());
        }
        System.out.println("Импорт отменён: " + summary.cancelled());
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private record ImportSummary(long totalLines, long validRecords, long totalErrors,
                                 List<ImportError> sampleErrors, boolean cancelled) {
    }

    private record ImportError(long lineNumber, String message) {
    }

    private static final class BoundedErrorCollector {
        private final int maxStored;
        private final List<ImportError> stored = new ArrayList<>();
        private long total;

        private BoundedErrorCollector(int maxStored) {
            this.maxStored = maxStored;
        }

        private void add(ImportError error) {
            total++;
            if (stored.size() < maxStored) {
                stored.add(error);
            }
        }

        private long totalErrors() {
            return total;
        }

        private List<ImportError> storedErrors() {
            return List.copyOf(stored);
        }
    }
}

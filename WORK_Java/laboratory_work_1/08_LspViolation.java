import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Задание 8. «Нарушение LSP».
 *
 * ReadOnlyCourseRepository реализует LegacyCourseRepository и бросает
 * UnsupportedOperationException из save(): сигнатура компилируется,
 * а поведение нарушает постусловие контракта. Исправление — разделить
 * «толстый» контракт на ReadableRepository и WritableRepository,
 * чтобы ошибка переехала из рантайма в компиляцию.
 *
 * Запуск: javac 08_LspViolation.java
 *         java  -cp . LspViolation
 */
class LspViolation {

    /**
     * Доменный объект «курс». Геттеры нужны всем реализациям репозиториев
     * для поиска по коду и toString() — для вывода в клиенте.
     */
    static class Course {

        private final String code;
        private final String title;

        /**
         * Создаёт курс. Вызывается из main при наполнении хранилища,
         * каталога и списка для проверки неизменяемой копии.
         */
        Course(String code, String title) {
            this.code = code;
            this.title = title;
        }

        /**
         * Возвращает шифр курса. Используется методом findByCode(...)
         * во всех реализациях репозиториев для сопоставления.
         */
        String getCode() {
            return code;
        }

        /**
         * Возвращает название курса. Используется в toString(),
         * который печатает CatalogPrinter.printAll().
         */
        String getTitle() {
            return title;
        }

        /**
         * Переопределяет Object.toString() краткой формой.
         * Используется клиентом CatalogPrinter при выводе каталога.
         */
        @Override
        public String toString() {
            return code + " '" + title + "'";
        }
    }

    /**
     * «Старый» толстый контракт хранилища: обещает и чтение, и запись.
     * Именно смешение независимых операций в одном интерфейсе и вызывает
     * нарушение LSP у read-only реализации.
     */
    interface LegacyCourseRepository {

        /**
         * Обещает сохранить курс. У InMemoryCourseRepository выполняется,
         * у ReadOnlyCourseRepository — нет, что и есть нарушение контракта.
         * Вызывается из main в сломанном и исправленном вариантах.
         */
        void save(Course course);

        /**
         * Возвращает все курсы. Вызывается из main для сравнения
         * содержимого честного и read-only хранилищ.
         */
        List<Course> findAll();

        /**
         * Возвращает курс по коду или Optional.empty().
         * Вызывается клиентами репозитория при точечном поиске.
         */
        Optional<Course> findByCode(String code);
    }

    /**
     * Честная реализация «толстого» контракта: и чтение, и запись
     * действительно работают, поэтому нарушения LSP нет.
     */
    static class InMemoryCourseRepository implements LegacyCourseRepository {

        private final List<Course> storage = new ArrayList<Course>();

        /**
         * Добавляет курс во внутренний список. Вызывается из main
         * при подготовке непустого хранилища.
         */
        @Override
        public void save(Course course) {
            storage.add(course);
        }

        /**
         * Возвращает копию списка курсов, чтобы клиент не получил
         * «живой» доступ к внутреннему состоянию. Вызывается из main
         * для сравнения с read-only репозиторием.
         */
        @Override
        public List<Course> findAll() {
            return new ArrayList<Course>(storage);
        }

        /**
         * Ищет курс по коду и возвращает Optional.of(...) либо
         * Optional.empty(). Вызывается клиентами репозитория.
         */
        @Override
        public Optional<Course> findByCode(String code) {
            for (Course c : storage) {
                if (c.getCode().equals(code)) {
                    return Optional.of(c);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Нарушение LSP: объявлена как полноценный репозиторий, но save()
     * бросает UnsupportedOperationException. Код клиента компилируется,
     * модульные тесты с InMemory-реализацией проходят, а падает прод.
     */
    static class ReadOnlyCourseRepository implements LegacyCourseRepository {

        private final List<Course> source = new ArrayList<Course>();

        /**
         * НАРУШЕНИЕ КОНТРАКТА: вместо сохранения бросает исключение.
         * Вызывается из main внутри try-catch, чтобы показать, что
         * ошибка обнаруживается только в рантайме.
         */
        @Override
        public void save(Course course) {
            throw new UnsupportedOperationException(
                    "репозиторий только для чтения, save() не поддерживается");
        }

        /**
         * Возвращает копию источника данных. Работает корректно —
         * сломан только метод записи. Вызывается из main при сравнении.
         */
        @Override
        public List<Course> findAll() {
            return new ArrayList<Course>(source);
        }

        /**
         * Ищет курс по коду в источнике данных. Поведение соответствует
         * контракту. Вызывается клиентами репозитория.
         */
        @Override
        public Optional<Course> findByCode(String code) {
            for (Course c : source) {
                if (c.getCode().equals(code)) {
                    return Optional.of(c);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Контракт ТОЛЬКО ЧТЕНИЯ. Не содержит save(), поэтому read-only
     * реализация физически не может его нарушить, а ошибка
     * перемещается на этап компиляции.
     */
    interface ReadableRepository {

        /**
         * Возвращает все доступные курсы. Вызывается клиентом
         * CatalogPrinter.printAll() и делегируется в AuditWriteRepository.
         */
        List<Course> findAll();

        /**
         * Возвращает курс по коду или Optional.empty().
         * Вызывается клиентами репозитория.
         */
        Optional<Course> findByCode(String code);
    }

    /**
     * Контракт ТОЛЬКО ЗАПИСИ. Вынесен отдельно, чтобы право записи
     * было у получателя явным, а не подразумеваемым.
     */
    interface WritableRepository {

        /**
         * Обещает сохранить курс. Реализации обязаны выполнить обещание:
         * InMemoryRepository пишет в storage, AuditWriteRepository — в журнал.
         */
        void save(Course course);
    }

    /**
     * Полный контракт хранилища: множественное наследование ИНТЕРФЕЙСОВ,
     * которое в Java законно. Содержит и чтение, и запись.
     */
    interface CourseRepository extends ReadableRepository, WritableRepository {
    }

    /**
     * Полноценная реализация нового контракта: и читает, и пишет,
     * нарушений нет. Используется в исправленном варианте в main.
     */
    static class InMemoryRepository implements CourseRepository {

        private final List<Course> storage = new ArrayList<Course>();

        /**
         * Сохраняет курс во внутреннем списке. Вызывается из main
         * при подготовке хранилища и клиентом CatalogPrinter-совместимых сценариев.
         */
        @Override
        public void save(Course course) {
            storage.add(course);
        }

        /**
         * Возвращает копию содержимого. Вызывается CatalogPrinter.printAll()
         * и клиентом main для печати размера неизменяемой копии.
         */
        @Override
        public List<Course> findAll() {
            return new ArrayList<Course>(storage);
        }

        /**
         * Ищет курс по коду. Вызывается клиентами репозитория.
         */
        @Override
        public Optional<Course> findByCode(String code) {
            for (Course c : storage) {
                if (c.getCode().equals(code)) {
                    return Optional.of(c);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Read-only источник данных: реализует ТОЛЬКО ReadableRepository,
     * поэтому метода save() у него не существует в принципе.
     */
    static class CourseCatalog implements ReadableRepository {

        private final List<Course> catalog = new ArrayList<Course>();

        /**
         * Наполняет каталог перечнем курсов (varargs: ноль и более).
         * Вызывается из main при создании каталога из трёх дисциплин.
         */
        CourseCatalog(Course... initial) {
            for (Course c : initial) {
                catalog.add(c);
            }
        }

        /**
         * Возвращает копию каталога. Вызывается CatalogPrinter.printAll()
         * и делегируется AuditWriteRepository.findAll().
         */
        @Override
        public List<Course> findAll() {
            return new ArrayList<Course>(catalog);
        }

        /**
         * Ищет курс по коду. Вызывается клиентами репозитория.
         */
        @Override
        public Optional<Course> findByCode(String code) {
            for (Course c : catalog) {
                if (c.getCode().equals(code)) {
                    return Optional.of(c);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Второй валидный приём: запись объявлена законно, потому что
     * действительно пишет — но в отдельный журнал, а читает из источника.
     * Постусловие «курс сохранён» выполнено.
     */
    static class AuditWriteRepository implements CourseRepository {

        private final ReadableRepository source;
        private final List<Course> writeJournal;

        /**
         * Принимает источник для чтения и журнал для записи.
         * Вызывается из main для проверки «запись реально работает».
         */
        AuditWriteRepository(ReadableRepository source, List<Course> writeJournal) {
            this.source = source;
            this.writeJournal = writeJournal;
        }

        /**
         * Пишет курс в оперативный журнал. Вызывается из main,
         * и размер журнала печатается как подтверждение выполнения
         * обещания контракта.
         */
        @Override
        public void save(Course course) {
            writeJournal.add(course);
        }

        /**
         * Делегирует чтение источнику. Вызывается CatalogPrinter.printAll()
         * в main, показывая, что чтение по-прежнему идёт из каталога.
         */
        @Override
        public List<Course> findAll() {
            return source.findAll();
        }

        /**
         * Делегирует поиск по коду источнику. Вызывается клиентами
         * репозитория.
         */
        @Override
        public Optional<Course> findByCode(String code) {
            return source.findByCode(code);
        }
    }

    /**
     * Клиент, которому достаточно только чтения. Зависимость от
     * ReadableRepository (минимальный набор прав) означает, что клиент
     * физически не может испортить данные.
     */
    static class CatalogPrinter {

        private final ReadableRepository readable;

        /**
         * Принимает любой источник чтения: и каталог, и полноценный
         * репозиторий, и аудиторскую обёртку. Вызывается из main трижды.
         */
        CatalogPrinter(ReadableRepository readable) {
            this.readable = readable;
        }

        /**
         * Печатает все курсы через readable.findAll().
         * Вызывается из main для каталога, репозитория и аудиторской
         * обёртки — код клиента ни разу не менялся.
         */
        void printAll() {
            for (Course c : readable.findAll()) {
                System.out.println("      * " + c);
            }
        }
    }

    /**
     * Точка входа. Сначала воспроизводит нарушение LSP (ошибка в рантайме),
     * затем показывает исправленный вариант с разделёнными контрактами,
     * доказывает невозможность вызова save() на уровне компиляции,
     * показывает рабочую декорированную запись и перечисляет два
     * допустимых, но менее чистых приёма.
     */
    public static void main(String[] args) {

        System.out.println("=== 1. СЛОМАННЫЙ ВАРИАНТ: ошибка поймана только в рантайме ===");
        InMemoryCourseRepository memory = new InMemoryCourseRepository();
        memory.save(new Course("CRS-101", "Основы Java"));
        memory.save(new Course("CRS-202", "Сетевые технологии"));

        ReadOnlyCourseRepository readOnly = new ReadOnlyCourseRepository();
        System.out.println("   readOnly.findAll() вернул " + readOnly.findAll().size() + " элементов (пусто)");
        System.out.println("   для сравнения, честное хранилище вернуло "
                + memory.findAll().size() + " элементов: " + memory.findAll());

        try {
            readOnly.save(new Course("CRS-303", "Компиляция"));
            System.out.println("   ??? не должно сюда попасть");
        } catch (UnsupportedOperationException e) {
            System.out.println("   Упало в рантайме: " + e.getMessage());
            System.out.println("   Компилятор был доволен: тип ReadOnlyCourseRepository");
            System.out.println("   ОБЕЩАЕТ метод save. Это и есть нарушение LSP.");
        }

        System.out.println();
        System.out.println("=== 2. ИСПРАВЛЕННЫЙ ВАРИАНТ: разделение контрактов ===");
        CourseCatalog catalog = new CourseCatalog(
                new Course("CRS-101", "Основы Java"),
                new Course("CRS-202", "Сетевые технологии"),
                new Course("CRS-303", "Базы данных"));
        InMemoryRepository repo = new InMemoryRepository();
        repo.save(new Course("CRS-101", "Основы Java"));

        System.out.println("   каталог (только чтение):");
        new CatalogPrinter(catalog).printAll();

        System.out.println("   хранилище (чтение+запись):");
        new CatalogPrinter(repo).printAll();

        System.out.println();
        System.out.println("=== 3. Доказательство: невозможный вызов НЕ КОМПИЛИРУЕТСЯ ===");
        ReadableRepository onlyRead = catalog;
        System.out.println("   Переменная объявлена как ReadableRepository:");
        System.out.println("   её класс: " + onlyRead.getClass().getSimpleName());
        System.out.println("   в контракте нет save(), поэтому вызов save() — ошибка компиляции.");
        System.out.println("       onlyRead.save(new Course(\"CRS-404\", \"X\"));");
        System.out.println("       error: cannot find symbol — method save(Course)");

        System.out.println();
        System.out.println("=== 4. Если запись всё-таки нужна — она РЕАЛЬНО работает ===");
        List<Course> writeJournal = new ArrayList<Course>();
        AuditWriteRepository audit = new AuditWriteRepository(catalog, writeJournal);
        audit.save(new Course("CRS-999", "Архивная версия"));
        System.out.println("   audit.save(...) отработал, размер журнала записи = "
                + writeJournal.size());
        System.out.println("   но findAll() у него по-прежнему читает каталог:");
        new CatalogPrinter(audit).printAll();

        System.out.println();
        System.out.println("=== 5. ЕЩЁ ДВА ПРИЁМА (и их цена) ===");
        System.out.println("   a) default-метод в контракте, бросающий исключение:");
        System.out.println("      выглядит элегантно (нет UnsupportedOperationException в коде),");
        System.out.println("      но это ТОТ ЖЕ обман: ошибка переносится в рантайм.");
        System.out.println("      Применять можно, если исключение — часть ДОКУМЕНТИРОВАННОГО");
        System.out.println("      контракта (как у Collections.unmodifiableList().add()).");
        System.out.println("   b) Collections.unmodifiableList(...) — «взрослый» вариант:");
        System.out.println("      возвращаем настоящую read-only КОПИЮ/ПРЕДСТАВЛЕНИЕ.");
        System.out.println("      Именно так работают List.of(...), Collections.emptyList().");
        List<Course> immutable = java.util.Collections.unmodifiableList(repo.findAll());
        System.out.println("      размер неизменяемого списка: " + immutable.size());
        try {
            immutable.add(new Course("CRS-000", "Nope"));
        } catch (UnsupportedOperationException e) {
            System.out.println("      immutable.add(...) -> UnsupportedOperationException");
            System.out.println("      (здесь это законно: метод ДОКУМЕНТИРОВАН как запрещённый)");
        }

        System.out.println();
        System.out.println("=== 6. ИТОГ ===");
        System.out.println("   Нарушение LSP: «объявил save, но бросаешь исключение».");
        System.out.println("   Исправление:  разделить контракт на ReadableRepository");
        System.out.println("                 и WritableRepository, чтобы read-only класс");
        System.out.println("                 не мог даже ПРОМЕЧТАТЬ о записи.");
        System.out.println("   Побочная выгода: зависимости клиента отражают реальные права,");
        System.out.println("   и часть ошибок ловится компилятором, а не продакшеном.");
    }
}

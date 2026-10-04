import java.util.Locale;

/**
 * Задание 6. «Переопределение без случайной перегрузки».
 *
 * Разбирает тихие ошибки, связанные с @Override: несовпадение параметров
 * (случайная перегрузка вместо переопределения), сужение доступа,
 * несовместимый возвращаемый тип, а также запреты на переопределение
 * final-, private- и static-методов. Показан реальный исторический баг
 * с equals(Locale) и его исправленный вариант с честным именем метода.
 *
 * Запуск: javac 06_OverrideVsOverload.java
 *         java  -cp . OverrideVsOverload
 */
class OverrideVsOverload {

    /**
     * Базовый класс: описывает объект, который умеют сохранить и найти.
     * Объявляет методы с корректными @Override в наследниках —
     * как эталон правильного оформления.
     */
    static class Repository {

        /**
         * Сохраняет объект по ключу и сообщает результат.
         * Вызывается из main напрямую у ссылки типа Repository
         * и из BadChildRepository.saveKey(...) через super.save(...).
         */
        public boolean save(String key, String payload) {
            System.out.println("      [Repository.save(String,String)] key=" + key);
            return true;
        }

        /**
         * Возвращает значение по ключу или null, если не найдено.
         * Вызывается из main у базовой ссылки и переопределяется
         * в BadChildRepository, который вызывает super.find(...).
         */
        public String find(String key) {
            return "value-of-" + key;
        }
    }

    /**
     * Наследник с исправленной ошибкой: метод, который автор хотел
     * переопределить, но параметр был другого типа. Такое объявление
     * молча компилировалось как перегрузка, поэтому метод переименован
     * в saveKey — связь с базой стала явной.
     */
    static class BadChildRepository extends Repository {

        /**
         * Раньше здесь было save(Repository key, String payload) —
         * законная перегрузка, а не переопределение, и @Override выдал бы
         * ошибку компиляции. Теперь метод честно отличается именем
         * и вызывает базовую логику. Вызывается из main у ссылки
         * типа BadChildRepository, тогда как bad.save(...) уходит
         * в унаследованный метод базы.
         */
        public boolean saveKey(String key, String payload) {
            System.out.println("      [BadChildRepository.saveKey] key=" + key);
            return super.save(key, payload);
        }

        /**
         * Корректное переопределение find: тип возврата совпадает с базовым,
         * поэтому @Override законен. Добавляет префикс к результату
         * super.find(...). Вызывается из main у ссылки типа
         * BadChildRepository, чтобы показать работу диспетчеризации.
         */
        @Override
        public String find(String key) {
            return "PREFIX:" + super.find(key);
        }
    }

    /**
     * Класс с историческим багом: метод equals(Locale) перегружает,
     * а не переопределяет Object.equals(Object). Код компилируется,
     * но вызов a.equals(Locale.US) уходит не в то место. Поставить
     * @Override здесь нельзя — компилятор сразу бы это обнаружил.
     */
    static class TextEntity {

        private final String text;
        private final Locale locale;

        /**
         * Создаёт сущность с текстом и локалью. Вызывается из main
         * при сравнении перегрузки и настоящего equals(Object).
         */
        TextEntity(String text, Locale locale) {
            this.text = text;
            this.locale = locale;
        }

        /**
         * Ошибочная перегрузка: сравнивает поле locale с аргументом.
         * Вызывается из main как a.equals(Locale.US) и намеренно
         * не является переопределением equals(Object).
         */
        public boolean equals(Locale locale) {
            return this.locale == locale;
        }

        /**
         * Настоящее переопределение equals(Object): сравнивает только текст.
         * Вызывается из main у a.equals(b) и a.equals("Java"),
         * а также платформой коллекций при поиске по HashSet/HashMap.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TextEntity)) {
                return false;
            }
            TextEntity other = (TextEntity) o;
            return text.equals(other.text);
        }

        /**
         * Хэш по тексту, обязательный парный equals.
         * Используется HashSet и HashMap при работе с TextEntity.
         */
        @Override
        public int hashCode() {
            return text.hashCode();
        }
    }

    /**
     * Исправленный вариант: перегрузки equals(Locale) нет вообще,
     * сравнение с локалью получило честное имя equalsInLocale(...).
     * Теперь каждый вызов однозначно сообщает, что он делает.
     */
    static class FixedTextEntity {

        private final String text;
        private final Locale locale;

        /**
         * Создаёт сущность с текстом и локалью. Вызывается из main
         * при демонстрации однозначного варианта без перегрузки equals.
         */
        FixedTextEntity(String text, Locale locale) {
            this.text = text;
            this.locale = locale;
        }

        /**
         * Сравнение с учётом локали под честным именем, поэтому
         * переопределением быть не может. Вызывается из main как
         * fa.equalsInLocale(Locale.US).
         */
        public boolean equalsInLocale(Locale other) {
            return locale == other;
        }

        /**
         * Настоящее переопределение equals(Object) по тексту.
         * Вызывается из main у fa.equals(fb) и платформой коллекций.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FixedTextEntity)) {
                return false;
            }
            FixedTextEntity other = (FixedTextEntity) o;
            return text.equals(other.text);
        }

        /**
         * Хэш по тексту, обязательный парный equals.
         * Используется HashSet и HashMap при работе с FixedTextEntity.
         */
        @Override
        public int hashCode() {
            return text.hashCode();
        }
    }

    /**
     * Класс с тихим багом другого рода: toString(int) — перегрузка,
     * а не переопределение Object.toString(). Смысл вызова размыт,
     * и компилятор не способен заметить подмену.
     */
    static class SloppyString {

        private final String value;

        /**
         * Создаёт обёртку над строкой. Вызывается из main при демонстрации
         * сосуществия toString() и toString(int).
         */
        SloppyString(String value) {
            this.value = value;
        }

        /**
         * Ошибочная перегрузка: возвращает первые maxLength символов.
         * Вызывается из main как s.toString(5); при изменении смысла
         * компилятор не заметил бы подмены.
         */
        public String toString(int maxLength) {
            return value.substring(0, Math.min(maxLength, value.length()));
        }

        /**
         * Настоящее переопределение toString(). Вызывается неявно
         * при печати объекта в main.
         */
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Базовый класс для демонстрации правил доступа при переопределении:
     * protected-метод расширяется до public в наследнике, но не наоборот.
     */
    static class BaseWithProtectedMethod {

        /**
         * protected-метод базы. Вызывается из WidenAccess.describe()
         * через super.describe(...) и из main косвенно.
         */
        protected String describe() {
            return "base";
        }

        /**
         * public-метод базы. Вызывается из WidenAccess.describePublic()
         * через super и напрямую из main.
         */
        public String describePublic() {
            return "base-public";
        }
    }

    /**
     * Наследник, РАСШИРЯЮЩИЙ доступ: protected становится public.
     * Сужение (public в базе -> protected в наследнике) дало бы
     * ошибку компиляции, поэтому здесь его нет.
     */
    static class WidenAccess extends BaseWithProtectedMethod {

        /**
         * Расширение доступа protected -> public: безопасное переопределение.
         * Вызывается из main у ссылки типа WidenAccess.
         */
        @Override
        public String describe() {
            return "widen:" + super.describe();
        }

        /**
         * Переопределение публичного метода базы с добавлением префикса.
         * Вызывается из main у ссылки типа WidenAccess.
         */
        @Override
        public String describePublic() {
            return "widen-public:" + super.describePublic();
        }
    }

    /**
     * Базовый класс с возвращаемым типом String: наследник может вернуть
     * String или его подтип, но не Object — иначе обещание базы нарушится.
     */
    static class BaseReturningString {

        /**
         * Возвращает метку базового типа. Вызывается из main косвенно:
         * при вызове через базовую ссылку выполняется версия наследника,
         * но тип результата остаётся String.
         */
        public String label() {
            return "base-label";
        }
    }

    /**
     * Наследник с тем же возвращаемым типом: ковариантность соблюдена,
     * @Override законен.
     */
    static class ChildReturningString extends BaseReturningString {

        /**
         * Возвращает метку наследника. Вызывается из main через
         * ссылку базового типа — dynamic dispatch выбирает именно это тело.
         */
        @Override
        public String label() {
            return "child-label";
        }
    }

    /**
     * Базовый класс с двумя неприступными стенами: final-метод frozen(),
     * который нельзя переопределить, и private-метод secret(),
     * который наследникам не достаётся вовсе.
     */
    static class BaseWithFinalAndPrivate {

        /**
         * final-метод: переопределение в наследнике запрещено компилятором.
         * Вызывается из main у ChildWithFinalAndPrivate, чтобы показать,
         * что используется версия базы.
         */
        public final String frozen() {
            return "frozen";
        }

        /**
         * private-метод базы: не наследуется. Метод с тем же именем
         * в наследнике — это новый, никак не связанный метод.
         * Вызывается изнутри base-класса методом callSecret().
         */
        private String secret() {
            return "base-secret";
        }

        /**
         * Публичная обёртка, вызывающая private-метод базы.
         * Вызывается из main у ChildWithFinalAndPrivate и всегда попадает
         * в base-secret, минуя одноимённый метод наследника.
         */
        public String callSecret() {
            return secret();
        }
    }

    /**
     * Наследник, который НЕ переопределяет frozen() (final) и заводит
     * собственный secret() (private-метод базы не наследуется).
     */
    static class ChildWithFinalAndPrivate extends BaseWithFinalAndPrivate {

        /**
         * Новый метод, а НЕ переопределение: у базы secret() private,
         * поэтому @Override здесь дал бы ошибку компиляции.
         * Вызывается напрямую из main у c.secret() и даёт
         * "child-not-override".
         */
        public String secret() {
            return "child-not-override";
        }
    }

    /**
     * Базовый класс со статическим методом: статические методы
     * не переопределяются, а скрываются, поэтому @Override неприменим.
     */
    static class BaseWithStatic {

        /**
         * Возвращает "base-static". Вызывается из main напрямую
         * через имя класса BaseWithStatic.
         */
        static String kind() {
            return "base-static";
        }
    }

    /**
     * Наследник, СКРЫВАЮЩИЙ статический метод базы: при вызове через
     * имя наследника выполняется версия этого класса.
     */
    static class ChildWithStatic extends BaseWithStatic {

        /**
         * Скрывает статический метод базы, а не переопределяет его.
         * Вызывается из main через имя класса ChildWithStatic и даёт
         * "child-static".
         */
        static String kind() {
            return "child-static";
        }
    }

    /**
     * Точка входа. Последовательно демонстрирует: тихую перегрузку вместо
     * переопределения, корректную работу find(), ловушку equals(Locale),
     * исправленный вариант, toString(int), расширение доступа,
     * ковариантность возвращаемого типа и поведение final/private/static.
     * В конце печатает памятку по @Override.
     */
    public static void main(String[] args) {

        System.out.println("=== 1. Ошибка в параметрах: тихая перегрузка ===");
        Repository good = new Repository();
        good.save("k1", "v1");
        BadChildRepository bad = new BadChildRepository();
        bad.saveKey("k2", "v2");
        bad.save("k3", "v3");
        System.out.println("   bad.save(...) отработал БАЗОВЫЙ метод — вот цена ошибки");

        System.out.println();
        System.out.println("=== 2. Переопределение find() работает как задумано ===");
        System.out.println("   base.find(\"x\") = " + good.find("x"));
        System.out.println("   bad.find(\"x\")  = " + bad.find("x"));

        System.out.println();
        System.out.println("=== 3. ЛОВУШКА equals(Locale): компилируется, но не то ===");
        TextEntity a = new TextEntity("Java", Locale.US);
        TextEntity b = new TextEntity("Java", Locale.FRANCE);
        System.out.println("   a.equals(Locale.US)     = " + a.equals(Locale.US)
                + "   <- вызвана ПЕРЕГРУЗКА (Locale), а не equals(Object)");
        System.out.println("   a.equals(b)             = " + a.equals(b)
                + "   <- вызван НАСТОЯЩИЙ equals(Object) из Object");
        System.out.println("   a.equals(\"Java\")       = " + a.equals("Java")
                + "   <- настоящий equals(Object): тип другой -> false");
        System.out.println("   ИТОГ: одинаково «похожие» объекты, разные результаты,");
        System.out.println("   и ни одной ошибки компиляции. Именно это ловит @Override.");

        System.out.println();
        System.out.println("=== 4. Исправленный вариант: никакой двусмысленности ===");
        FixedTextEntity fa = new FixedTextEntity("Java", Locale.US);
        FixedTextEntity fb = new FixedTextEntity("Java", Locale.US);
        System.out.println("   fa.equalsInLocale(Locale.US) = " + fa.equalsInLocale(Locale.US));
        System.out.println("   fa.equals(fb)                = " + fa.equals(fb));

        System.out.println();
        System.out.println("=== 5. toString(int) — та же ошибка другими словами ===");
        SloppyString s = new SloppyString("Полиморфизм");
        System.out.println("   s.toString()      = " + s);
        System.out.println("   s.toString(5)     = " + s.toString(5));
        System.out.println("   Оба метода сосуществуют. Если бы кто-то написал");
        System.out.println("   String x = s.toString(5) и ожидал substr — он его получил.");
        System.out.println("   Но если бы сигнатура стала toString() с другим смыслом —");
        System.out.println("   компилятор бы не заметил подмены.");

        System.out.println();
        System.out.println("=== 6. Доступ: расширять можно, сужать нельзя ===");
        WidenAccess w = new WidenAccess();
        System.out.println("   w.describe()       = " + w.describe()
                + "   (protected в базе -> public в наследнике: ОК)");
        System.out.println("   w.describePublic() = " + w.describePublic());

        System.out.println();
        System.out.println("=== 7. Возвращаемый тип: ковариантность ===");
        BaseReturningString brs = new ChildReturningString();
        System.out.println("   через базовую ссылку: " + brs.label()
                + "   <- тип String гарантирован, тело — из наследника");

        System.out.println();
        System.out.println("=== 8. final и private: две неприступные стены ===");
        ChildWithFinalAndPrivate c = new ChildWithFinalAndPrivate();
        System.out.println("   c.callSecret() = " + c.callSecret()
                + "   <- вызов внутри базы идёт в private-метод БАЗЫ");
        System.out.println("   c.secret()      = " + c.secret()
                + "   <- прямой вызов попадает в НОВЫЙ метод наследника");
        System.out.println("   c.frozen()      = " + c.frozen()
                + "   <- final-метод базы, переопределить нельзя");
        System.out.println("   BaseWithStatic.kind()      = " + BaseWithStatic.kind());
        System.out.println("   ChildWithStatic.kind()     = " + ChildWithStatic.kind()
                + "   <- static скрывается, а не переопределяется");

        System.out.println();
        System.out.println("=== 9. ИТОГ: памятка по @Override ===");
        System.out.println("   @Override ставим, когда:");
        System.out.println("     • реализуем метод интерфейса (Java 6+ разрешает и это);");
        System.out.println("     • переопределяем метод суперкласса;");
        System.out.println("   НЕ ставим, когда:");
        System.out.println("     • это перегрузка (другая сигнатура) — и это нормально;");
        System.out.println("     • это новый метод, которого не было у родителя;");
        System.out.println("     • это static/private/final — там @Override неприменим.");
    }
}

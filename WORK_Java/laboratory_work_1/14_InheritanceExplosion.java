import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Задание 14. «Наследование взрывает комбинации».
 *
 * Задача файла: отказаться от девяти подклассов для комбинаций форматов
 * (Online, Classroom, Hybrid) и политик оплаты (Free, Fixed, Subscription),
 * разделив независимые оси на компоненты, и показать, что добавление
 * нового формата или новой политики требует минимальных изменений.
 *
 * Почему девять подклассов — ловушка: две независимые оси по три значения
 * дают 3 x 3 = 9 классов, в каждом дублируется логика расписания и расчёта
 * цены, а добавление четвёртого значения по любой оси порождает новые
 * подклассы и правки существующих. Общий код приходится тащить вверх
 * по иерархии, и она превращается в кашу («иерархическое раздувание»).
 * Правильный ход: увидеть две независимые оси и заменить комбинации
 * компонентами.
 *
 * Важная оговорка: композиция не всегда лучше. Если оси связаны по смыслу,
 * их разумно объединить в один класс. Композиция оправдана, когда оси
 * действительно независимы.
 *
 * Запуск: javac 14_InheritanceExplosion.java
 *         java -cp . InheritanceExplosion
 *
 * Требования к среде: JDK 8+.
 */
class InheritanceExplosion {

    /**
     * Ось 1: «как проходит обучение». Это поведение, а не данные: у каждого
     * формата свой алгоритм расписания, свои ограничения и свой текст
     * инструкции. Правила касаются только формата и ничего не знают
     * об оплате — именно эта независимость позволяет разделить оси.
     */
    interface CourseFormat {

        /**
         * Возвращает имя формата. Используется в Course.toString(),
         * в describeFor(...) и при печати счёта изменений.
         */
        String name();

        /**
         * Возвращает описание формата для абитуриента. Вызывается
         * из Course.describeFor(...).
         */
        String description();

        /**
         * Возвращает число академических часов в неделю. Вызывается
         * из Course.describeFor(...).
         */
        int weeklyHours();

        /**
         * Возвращает строку расписания, вычисленную по правилам формата.
         * Вызывается из Course.describeFor(...).
         */
        String schedule();

        /**
         * Сообщает, допускает ли формат обязательное очное присутствие.
         * Правило касается только формата. Используется клиентом при
         * проверке допуска к лабораторным.
         */
        boolean supportsMandatoryAttendance();
    }

    /**
     * Формат 1: онлайн. Платформа передаётся как данные, поэтому отдельный
     * класс на каждый LMS не заводится.
     */
    static class OnlineFormat implements CourseFormat {

        private final String platform;

        /**
         * Задаёт платформу как данные. Вызывается в main и в
         * CourseCatalog.fromConfig(...) при сборке каталога из конфигурации.
         */
        OnlineFormat(String platform) {
            this.platform = platform;
        }

        /**
         * Возвращает имя "ONLINE". Используется в toString и describeFor(...).
         */
        @Override
        public String name() {
            return "ONLINE";
        }

        /**
         * Возвращает описание с указанием платформы. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String description() {
            return "дистанционно на платформе " + platform;
        }

        /**
         * Возвращает 6 академических часов в неделю. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public int weeklyHours() {
            return 6;
        }

        /**
         * Возвращает расписание вебинаров. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String schedule() {
            return "вебинары 2 раза в неделю, записи доступны всегда";
        }

        /**
         * Всегда false: очных занятий нет. Используется клиентом при
         * проверке допуска к лабораторным.
         */
        @Override
        public boolean supportsMandatoryAttendance() {
            return false;
        }
    }

    /**
     * Формат 2: очный. Корпус и аудитория — данные конструктора,
     * а не повод заводить подкласс.
     */
    static class ClassroomFormat implements CourseFormat {

        private final String building;

        /**
         * Задаёт корпус и аудиторию как данные. Вызывается в main
         * и в CourseCatalog.fromConfig(...) при сборке каталога.
         */
        ClassroomFormat(String building) {
            this.building = building;
        }

        /**
         * Возвращает имя "CLASSROOM". Используется в toString, describeFor(...)
         * и при печати комбинаций с новой политикой оплаты.
         */
        @Override
        public String name() {
            return "CLASSROOM";
        }

        /**
         * Возвращает описание с указанием корпуса и аудитории. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String description() {
            return "очно, " + building;
        }

        /**
         * Возвращает 8 академических часов в неделю. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public int weeklyHours() {
            return 8;
        }

        /**
         * Возвращает расписание по парам. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String schedule() {
            return "занятия по расписанию пар, 4 пары в неделю";
        }

        /**
         * Всегда true: формат предполагает очное присутствие. Используется
         * клиентом при проверке допуска к лабораторным.
         */
        @Override
        public boolean supportsMandatoryAttendance() {
            return true;
        }
    }

    /**
     * Формат 3: гибридный. Является смесью двух правил, поэтому смесь удобно
     * выразить композицией двух форматов, а не копированием их кода
     * в новый класс.
     */
    static class HybridFormat implements CourseFormat {

        private final int lecturesPerWeek;

        /**
         * Задаёт число очных лекций в неделю как данные. Вызывается в main
         * и в CourseCatalog.fromConfig(...) при сборке каталога.
         */
        HybridFormat(int lecturesPerWeek) {
            this.lecturesPerWeek = lecturesPerWeek;
        }

        /**
         * Возвращает имя "HYBRID". Используется в toString, describeFor(...)
         * и в конфигурации под ключом "hybrid".
         */
        @Override
        public String name() {
            return "HYBRID";
        }

        /**
         * Возвращает описание смешанного формата с числом лекций.
         * Вызывается из Course.describeFor(...).
         */
        @Override
        public String description() {
            return "смешанно: " + lecturesPerWeek + " очных лекции + онлайн-практика";
        }

        /**
         * Возвращает сумму часов лекций и онлайн-практики. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public int weeklyHours() {
            return 4 + 2;
        }

        /**
         * Возвращает расписание лекций с указанием остатка онлайна.
         * Вызывается из Course.describeFor(...).
         */
        @Override
        public String schedule() {
            return lecturesPerWeek + " очных лекции, остальное — онлайн";
        }

        /**
         * Всегда true: очные лекции входят в формат. Используется клиентом
         * при проверке допуска к лабораторным.
         */
        @Override
        public boolean supportsMandatoryAttendance() {
            return true;
        }
    }

    /**
     * Ось 2: «сколько и как платить». Тоже поведение — формула цены
     * и признак необходимости предоплаты, поэтому тоже интерфейс.
     */
    interface PaymentPolicy {

        /**
         * Возвращает имя политики. Используется в Course.toString(),
         * в describeFor(...) и при поиске курса с подпиской.
         */
        String name();

        /**
         * Возвращает стоимость курса для конкретного студента: цена может
         * зависеть от льгот, поэтому студент передаётся аргументом.
         * Вызывается из Course.describeFor(...) и из main при печати скидки.
         */
        int priceFor(Student student);

        /**
         * Сообщает, нужно ли оплатить всё сразу при зачислении.
         * Вызывается из Course.describeFor(...) для пометки в счёте.
         */
        boolean requiresFullPrepayment();

        /**
         * Возвращает человекочитаемое описание условий оплаты. Вызывается
         * из Course.describeFor(...).
         */
        String terms();
    }

    /**
     * Политика 1: бесплатно. Аргумент студента не нужен, но подпись
     * обязана совпадать с контрактом.
     */
    static class FreePolicy implements PaymentPolicy {

        /**
         * Возвращает имя "FREE". Используется в toString, describeFor(...)
         * и как ключ в реестре политик каталога.
         */
        @Override
        public String name() {
            return "FREE";
        }

        /**
         * Всегда возвращает ноль независимо от студента. Вызывается
         * из Course.describeFor(...) и main.
         */
        @Override
        public int priceFor(Student student) {
            return 0;
        }

        /**
         * Всегда false: предоплата не нужна. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public boolean requiresFullPrepayment() {
            return false;
        }

        /**
         * Возвращает условия бесплатного обучения. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String terms() {
            return "оплата не требуется";
        }
    }

    /**
     * Политика 2: фиксированная цена. Сумма — данные конструктора,
     * поэтому один класс покрывает любую цену.
     */
    static class FixedPricePolicy implements PaymentPolicy {

        private final int price;

        /**
         * Задаёт цену курса как данные. Вызывается в main и
         * в CourseCatalog.fromConfig(...) при сборке каталога.
         */
        FixedPricePolicy(int price) {
            this.price = price;
        }

        /**
         * Возвращает имя "FIXED". Используется в toString, describeFor(...)
         * и как ключ в реестре политик каталога.
         */
        @Override
        public String name() {
            return "FIXED";
        }

        /**
         * Возвращает цену, не зависящую от студента. Вызывается
         * из Course.describeFor(...) и main.
         */
        @Override
        public int priceFor(Student student) {
            return price;
        }

        /**
         * Всегда true: полная предоплата при зачислении. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public boolean requiresFullPrepayment() {
            return true;
        }

        /**
         * Возвращает условия оплаты с указанием суммы. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String terms() {
            return price + " руб. при зачислении";
        }
    }

    /**
     * Политика 3: подписка. Цена зависит от студента — ещё одна причина
     * держать это отдельным объектом со своим состоянием, а не флагом.
     */
    static class SubscriptionPolicy implements PaymentPolicy {

        private final int monthlyPrice;
        private final int discountPercent;

        /**
         * Задаёт месячную цену и процент скидки как данные. Вызывается
         * в main и в CourseCatalog.fromConfig(...) при сборке каталога.
         */
        SubscriptionPolicy(int monthlyPrice, int discountPercent) {
            this.monthlyPrice = monthlyPrice;
            this.discountPercent = discountPercent;
        }

        /**
         * Возвращает имя "SUBSCRIPTION". Используется в toString,
         * в describeFor(...) и при поиске курса с подпиской в main.
         */
        @Override
        public String name() {
            return "SUBSCRIPTION";
        }

        /**
         * Возвращает месячную цену, уменьшенную на скидку для льготников.
         * Логика скидки живёт только здесь. Вызывается
         * из Course.describeFor(...) и из main при печате сравнения цен.
         */
        @Override
        public int priceFor(Student student) {
            int base = monthlyPrice;
            if (student.hasBenefit()) {
                base = base - base * discountPercent / 100;
            }
            return base;
        }

        /**
         * Всегда false: оплата помесячная. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public boolean requiresFullPrepayment() {
            return false;
        }

        /**
         * Возвращает условия подписки с ценой и размером скидки.
         * Вызывается из Course.describeFor(...).
         */
        @Override
        public String terms() {
            return monthlyPrice + " руб./мес., скидка " + discountPercent + "% для льготников";
        }
    }

    /**
     * Студент: нужен политикам оплаты для расчёта цены. Всё, что о нём
     * знает движок, — идентификатор и признак льготы.
     */
    static class Student {

        private final String id;
        private final boolean benefit;

        /**
         * Сохраняет идентификатор и признак льготы. Вызывается из main
         * при создании обычного студента и льготника.
         */
        Student(String id, boolean benefit) {
            this.id = id;
            this.benefit = benefit;
        }

        /**
         * Возвращает идентификатор студента. Используется при печати
         * и формировании кодов курсов в main.
         */
        String getId() {
            return id;
        }

        /**
         * Сообщает, является ли студент льготником. Вызывается
         * из SubscriptionPolicy.priceFor(Student) для расчёта скидки.
         */
        boolean hasBenefit() {
            return benefit;
        }
    }

    /**
     * Курс больше не подтип каждого формата и каждой политики — он содержит
     * по одному объекту каждой оси. Отсюда 3 x 3 = 9 комбинаций без девяти
     * классов: классов всего 7 (3 формата, 3 политики и сам Course),
     * а комбинаций сколько угодно.
     */
    static class Course {

        private final String code;
        private final String title;
        private final CourseFormat format;
        private final PaymentPolicy payment;

        /**
         * Единственный конструктор. Типы параметров — интерфейсы,
         * поэтому курс не знает, какой именно формат и какая политика
         * ему даны. Вызывается в main при сборке комбинаций и
         * в CourseCatalog.fromConfig(...) при разборе конфигурации.
         */
        Course(String code, String title, CourseFormat format, PaymentPolicy payment) {
            this.code = code;
            this.title = title;
            this.format = format;
            this.payment = payment;
        }

        /**
         * Возвращает код курса. Используется в toString и при печати
         * каталога в main.
         */
        String getCode() {
            return code;
        }

        /**
         * Возвращает название курса. Используется в describeFor(...)
         * при печати счёта для абитуриента.
         */
        String getTitle() {
            return title;
        }

        /**
         * Возвращает компонент формата. Используется в main при поиске
         * курса и клиентом при проверке очного присутствия.
         */
        CourseFormat getFormat() {
            return format;
        }

        /**
         * Возвращает компонент оплаты. Используется в main при поиске
         * курса с подпиской и вызове priceFor(Student).
         */
        PaymentPolicy getPayment() {
            return payment;
        }

        /**
         * Клиентский метод: спрашивает у компонентов, не разбирая их типы.
         * Ни одного if по типу формата или оплаты. Вызывается из main
         * для обычного курса, для курса с новым форматом и для комбинаций
         * нового формата с новой политикой.
         */
        public String describeFor(Student student) {
            StringBuilder sb = new StringBuilder();
            sb.append(code).append(" '").append(title).append("'\n");
            sb.append("   формат : ").append(format.name()).append(" — ")
                    .append(format.description()).append('\n');
            sb.append("   распис. : ").append(format.schedule())
                    .append(" (").append(format.weeklyHours()).append(" ч/нед)\n");
            sb.append("   оплата  : ").append(payment.name()).append(" — ")
                    .append(payment.terms()).append('\n');
            sb.append("   цена    : ").append(payment.priceFor(student))
                    .append(payment.requiresFullPrepayment()
                            ? " (полная предоплата)" : " (можно частями)")
                    .append('\n');
            return sb.toString();
        }

        /**
         * Возвращает компактную форму «код[формат+оплата]». Вызывается
         * при печати всех комбинаций и каталога из конфигурации.
         */
        @Override
        public String toString() {
            return code + "[" + format.name() + "+" + payment.name() + "]";
        }
    }

    /**
     * Каталог курсов — «сборочный цех». Здесь и только здесь строки
     * конфигурации превращаются в объекты; всё остальное работает с типами.
     */
    static class CourseCatalog {

        private final List<Course> courses = new ArrayList<Course>();

        /**
         * Добавляет готовый курс в каталог. Вызывается из main при сборке
         * комбинаций и из fromConfig(...) для каждой строки конфигурации.
         */
        void add(Course course) {
            courses.add(course);
        }

        /**
         * Возвращает копию списка курсов. Вызывается из main при печати
         * каталога, собранного из конфигурации.
         */
        List<Course> all() {
            return new ArrayList<Course>(courses);
        }

        /**
         * Строит каталог из пар «код -> строка формата, код -> строка оплаты».
         * Соответствия «строка -> объект» заполняются в одном месте —
         * та же идея, что и граница в задании 13. Неизвестные значения
         * отвергаются сразу, при загрузке конфигурации. Вызывается
         * из main для трёх строк конфигурации.
         */
        static CourseCatalog fromConfig(List<String[]> config) {
            Map<String, CourseFormat> formats = new HashMap<String, CourseFormat>();
            formats.put("online", new OnlineFormat("Moodle"));
            formats.put("classroom", new ClassroomFormat("корпус 1, ауд. 305"));
            formats.put("hybrid", new HybridFormat(2));

            Map<String, PaymentPolicy> payments = new HashMap<String, PaymentPolicy>();
            payments.put("free", new FreePolicy());
            payments.put("fixed", new FixedPricePolicy(45000));
            payments.put("subscription", new SubscriptionPolicy(6000, 20));

            CourseCatalog catalog = new CourseCatalog();
            for (String[] row : config) {
                CourseFormat format = formats.get(row[2]);
                PaymentPolicy payment = payments.get(row[3]);
                if (format == null || payment == null) {
                    throw new IllegalArgumentException("неизвестная конфигурация: " + row[2] + "/" + row[3]);
                }
                catalog.add(new Course(row[0], row[1], format, payment));
            }
            return catalog;
        }
    }

    /**
     * Точка входа: строит все девять комбинаций из семи классов, показывает
     * работу одного клиентского метода для всех курсов, скидку подписки,
     * сборку каталога из строк конфигурации, добавление нового формата
     * и новой политики оплаты, счёт изменений и признаки, по которым
     * наследование всё-таки предпочтительнее.
     */
    public static void main(String[] args) {

        Student regular = new Student("STU-1", false);
        Student benefit = new Student("STU-2", true);

        System.out.println("=== 1. ВСЕ 9 КОМБИНАЦИЙ БЕЗ ЕДИНОГО НОВОГО КЛАССА ===");
        CourseFormat[] formats = {
                new OnlineFormat("Moodle"),
                new ClassroomFormat("корпус 1, ауд. 305"),
                new HybridFormat(2)
        };
        PaymentPolicy[] payments = {
                new FreePolicy(),
                new FixedPricePolicy(45000),
                new SubscriptionPolicy(6000, 20)
        };

        List<Course> catalog = new ArrayList<Course>();
        int counter = 1;
        for (CourseFormat f : formats) {
            for (PaymentPolicy p : payments) {
                catalog.add(new Course("CRS-" + (100 + counter), "Курс " + counter, f, p));
                counter++;
            }
        }
        System.out.println("   построено курсов: " + catalog.size() + " (3 формата x 3 оплаты)");
        System.out.println("   классов при этом: 3 + 3 + 1 = 7, а не 9");
        for (Course c : catalog) {
            System.out.println("      " + c);
        }

        System.out.println();
        System.out.println("=== 2. КЛИЕНТ НЕ ЗНАЕТ ТИПОВ КОМПОНЕНТОВ ===");
        System.out.print(catalog.get(0).describeFor(regular));
        System.out.print(catalog.get(4).describeFor(regular));

        System.out.println();
        System.out.println("=== 3. СКИДКА ПОДПИСКИ (зависимость от студента) ===");
        Course subscriptionCourse = null;
        for (Course c : catalog) {
            if (c.getPayment().name().equals("SUBSCRIPTION")) {
                subscriptionCourse = c;
                break;
            }
        }
        System.out.println("   обычный студент: " + subscriptionCourse.getPayment().priceFor(regular));
        System.out.println("   льготник:       " + subscriptionCourse.getPayment().priceFor(benefit));
        System.out.println("   Логика скидки живёт В КОМПОНЕНТЕ оплаты, а не в курсе и не в клиенте.");

        System.out.println();
        System.out.println("=== 4. СБОРКА ИЗ КОНФИГУРАЦИИ (строки только здесь) ===");
        List<String[]> config = new ArrayList<String[]>();
        config.add(new String[]{"CFG-1", "Программирование (онлайн)", "online", "free"});
        config.add(new String[]{"CFG-2", "Программирование (очно)", "classroom", "fixed"});
        config.add(new String[]{"CFG-3", "Сети (смешанно)", "hybrid", "subscription"});
        CourseCatalog fromConfig = CourseCatalog.fromConfig(config);
        for (Course c : fromConfig.all()) {
            System.out.println("   " + c);
        }
        System.out.println("   Строки 'online'/'fixed' нигде, кроме этого метода, не встречаются.");

        System.out.println();
        System.out.println("=== 5. ДОБАВЛЯЕМ НОВЫЙ ФОРМАТ: минимум изменений ===");
        System.out.println("   Меняем ТОЛЬКО: (а) добавляем класс, (б) одну строку в каталог.");
        System.out.println("   Не трогаем: Course, PaymentPolicy, все 3 старых формата,");
        System.out.println("   все 3 политики, все клиенты и циклы обхода.");
        System.out.println();
        System.out.println("   см. NewOnDemandFormat ниже и его подключение в шаге 6.");

        System.out.println();
        System.out.println("=== 6. НОВЫЙ ФОРМАТ РАБОТАЕТ ВМЕСТЕ СО ВСЕМИ ОПЛАТАМИ ===");
        CourseFormat onDemand = new NewOnDemandFormat();
        Course hybridOnDemand = new Course("CRS-NEW", "Новый курс (по требованию)", onDemand, payments[1]);
        System.out.print(hybridOnDemand.describeFor(regular));

        Course newCombo = new Course("CRS-NEW-2", "Новый курс, рассрочка", onDemand, new InstallmentPolicy(45000, 6));
        System.out.print(newCombo.describeFor(regular));

        System.out.println();
        System.out.println("=== 7. ДОБАВЛЯЕМ НОВУЮ ОПЛАТУ: тоже один класс ===");
        for (CourseFormat f : formats) {
            Course c = new Course("CRS-I", "Рассрочка + " + f.name(), f, new InstallmentPolicy(45000, 6));
            System.out.println("   " + c);
        }

        System.out.println();
        System.out.println("=== 8. СЧЁТ ИЗМЕНЕНИЙ ===");
        System.out.println("   Наследование (9 подклассов):");
        System.out.println("     +1 формат  ->  +3 класса, правки в существующих");
        System.out.println("     +1 оплата  ->  +3 класса, правки в существующих");
        System.out.println("     +1 и то, и то -> +9 классов");
        System.out.println();
        System.out.println("   Композиция (2 интерфейса + класс Course):");
        System.out.println("     +1 формат  ->  +1 класс, +1 строка в каталоге");
        System.out.println("     +1 оплата  ->  +1 класс, +1 строка в каталоге");
        System.out.println("     +1 и то, и то -> +2 класса, +2 строки, 0 правок в старом коде");
        System.out.println();
        System.out.println("   Правило, которое за этим стоит: добавляем ОСЬ, а не КОМБИНАЦИЮ.");
        System.out.println("   Комбинации появляются САМИ, в момент выполнения.");

        System.out.println();
        System.out.println("=== 9. КОГДА НАСЛЕДОВАНИЕ ВСЁ-ТАКИ ЛУЧШЕ ===");
        System.out.println("   Если оси СВЯЗАНЫ по смыслу и вариантов мало:");
        System.out.println("     • «Всегда бесплатно» и «всегда с предоплатой» — разные");
        System.out.println("       классы в иерархии — это нормально и дешевле;");
        System.out.println("     • если один вариант ВСЕГДА включает другой");
        System.out.println("       (Subscription = Fixed + «ежемесячно»), наследование");
        System.out.println("       моделирует это естественно;");
        System.out.println("     • иерархия из 3 классов с общим кодом читается лучше,");
        System.out.println("       чем две фабрики и объект-держатель.");
        System.out.println("   Признак, что пора переходить на композицию:");
        System.out.println("     • >3 значений на оси И оси независимы;");
        System.out.println("     • появился класс вида OnlineSubscriptionFreeCourse;");
        System.out.println("     • один и тот же кусок кода копируется между подклассами.");
    }

    /**
     * Новый формат, добавленный после того, как весь остальной код
     * был написан. Ни одно существующее предложение не пострадало:
     * реализуется контракт CourseFormat, и этого достаточно.
     */
    static class NewOnDemandFormat implements CourseFormat {

        /**
         * Возвращает имя "ON_DEMAND". Используется в toString, describeFor(...)
         * и в комбинациях со старой и новой политиками оплаты.
         */
        @Override
        public String name() {
            return "ON_DEMAND";
        }

        /**
         * Возвращает описание формата со свободным графиком. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String description() {
            return "записи лекций, свободный график";
        }

        /**
         * Всегда 0: фиксированных часов нет. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public int weeklyHours() {
            return 0;
        }

        /**
         * Возвращает пояснение об отсутствии расписания. Вызывается
         * из Course.describeFor(...).
         */
        @Override
        public String schedule() {
            return "без расписания: смотрите, когда удобно";
        }

        /**
         * Всегда false: очное присутствие не требуется. Используется клиентом
         * при проверке допуска к лабораторным.
         */
        @Override
        public boolean supportsMandatoryAttendance() {
            return false;
        }
    }

    /**
     * Новая политика оплаты: рассрочка. Это не подкласс FixedPricePolicy,
     * а отдельная стратегия, потому что правила другие — платёж частями,
     * а не целиком. Наследование с добавленным флагом installments было бы
     * «подклассом с флагом», то есть запахом кода.
     */
    static class InstallmentPolicy implements PaymentPolicy {

        private final int totalPrice;
        private final int months;

        /**
         * Задаёт полную сумму и число месяцев как данные. Вызывается в main
         * для комбинаций со всеми форматами.
         */
        InstallmentPolicy(int totalPrice, int months) {
            this.totalPrice = totalPrice;
            this.months = months;
        }

        /**
         * Возвращает имя "INSTALLMENTS". Используется в toString,
         * describeFor(...) и при печати комбинаций со всеми форматами.
         */
        @Override
        public String name() {
            return "INSTALLMENTS";
        }

        /**
         * Возвращает полную сумму: расрочка меняет график платежей,
         * а не итоговую цену. Вызывается из Course.describeFor(...).
         */
        @Override
        public int priceFor(Student student) {
            return totalPrice;
        }

        /**
         * Всегда false: оплата частями. Вызывается
         * из Course.describeFor(...) для пометки «можно частями».
         */
        @Override
        public boolean requiresFullPrepayment() {
            return false;
        }

        /**
         * Возвращает условия рассрочки с суммой и числом месяцев.
         * Вызывается из Course.describeFor(...).
         */
        @Override
        public String terms() {
            return totalPrice + " руб. за " + months + " мес.";
        }
    }
}
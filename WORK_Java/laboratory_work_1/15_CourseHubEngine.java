import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Задание 15. «Расширяемый движок правил» (CourseHub enrollment engine).
 *
 * Задача файла: собрать движок зачисления, где правила возвращают
 * типизированный результат из закрытого набора вариантов, контракт правила
 * остаётся открытым интерфейсом, композиция поддерживает allOf и anyOf,
 * сервис не проверяет конкретные классы (нет instanceof и switch по типу
 * правила), реализаций правил не меньше пяти, а dynamic dispatch
 * демонстрируется явно.
 *
 * Ключевые решения схемы: CourseHub знает только EnrollmentRule,
 * ClockProvider и Portal — упоминаний конкретных правил в нём нет ни одного,
 * поэтому зависимости направлены к абстракциям, а не вниз. EnrollmentRule —
 * открытый контракт, потому что это точка расширения системы для чужих
 * команд. EnrollmentDecision и RuleVerdict — закрытые контракты, потому что
 * набор исходов известен, и их исчерпывающая обработка проверяется
 * компилятором. Правила не знают друг о друге: их объединяют CourseHub
 * последовательно и композиционные правила AllOfRule и AnyOfRule.
 * EnrollmentContext — единственное место, откуда правила берут данные;
 * правило не ходит в базу само, это делает Portal.
 *
 * Запуск: javac --release 21 15_CourseHubEngine.java
 *         java -cp . CourseHubEngine
 *
 * Требования к среде: JDK 21 — нужны sealed-интерфейсы, record и switch
 * по типам (pattern matching for switch появился в Java 21).
 * Остальные задания 01...11 и 13...14 собираются на JDK 8 и новее.
 */
class CourseHubEngine {

    /**
     * Студент. Record — неизменяемый класс-значение с автоматическими
     * equals и hashCode. Поле сданных дисциплин имеет тип SetOfCodes:
     * компактная обёртка ради читаемости примера, в реальном коде
     * здесь был бы java.util.Set.
     */
    record Student(String id, int age, SetOfCodes completed, int credits, boolean resident) {
    }

    /**
     * Простейшая обёртка «множество кодов»: хранит массив строк и умеет
     * отвечать, есть ли в нём такой код. Нужна, чтобы пример оставался
     * компактным и понятным новичку.
     */
    static final class SetOfCodes {

        private final String[] codes;

        /**
         * Запоминает переданные коды; поле final, поэтому значение
         * не меняется. Вызывается при создании студентов в main.
         */
        SetOfCodes(String... codes) {
            this.codes = codes;
        }

        /**
         * Сообщает, содержит ли множество указанный код. Строки сравниваются
         * по значению, а не по ссылке. Вызывается из PrerequisiteRule.check(...)
         * для каждого пререквизита курса.
         */
        boolean contains(String code) {
            for (String c : codes) {
                if (c.equals(code)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Возвращает число кодов. Используется клиентом при печати
         * сведений о студенте.
         */
        int size() {
            return codes.length;
        }

        /**
         * Возвращает представление списка кодов в квадратных скобках.
         * Вызывается из PrerequisiteRule.check(...) при формировании
         * пояснения вердикта.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < codes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(codes[i]);
            }
            return sb.append("]").toString();
        }
    }

    /**
     * Курс: неизменяемый класс-значение с кодом, названием, требуемыми
     * кредитами, массивом пререквизитов и вместимостью.
     */
    record Course(String code, String title, int requiredCredits,
                  String[] prerequisiteCodes, int capacity) {
    }

    /**
     * Portal — порт наружу, шлюз к внешним системам. Правила не ходят
     * в базу напрямую, только через этот интерфейс, поэтому движок
     * ничего не знает о базе данных и легко тестируется.
     */
    interface Portal {

        /**
         * Возвращает число занятых мест по курсу. Вызывается из
         * CapacityRule.check(...) для сравнения с вместимостью.
         */
        int seatsTaken(String courseCode);

        /**
         * Сообщает, утверждён ли индивидуальный учебный план студента.
         * Вызывается из DisciplinePlanRule.check(...).
         */
        boolean hasDisciplinePlan(String studentId);

        /**
         * Записывает факт зачисления во внешнюю систему. Вызывается сервисом
         * ровно один раз — при принятом решении. Правила этот метод
         * не вызывают: запись является операцией сервиса, а не проверкой,
         * поэтому правила не могут менять состояние.
         */
        void registerEnrollment(String studentId, String courseCode);
    }

    /**
     * Источник времени. Нужен, чтобы правило «окно записи» зависело
     * от внедрённых часов, а не от System.currentTimeMillis(), иначе
     * тесты были бы хрупкими.
     */
    interface ClockProvider {

        /**
         * Возвращает текущий момент времени. Вызывается правилами,
         * которым нужно знать «сейчас», и тестами.
         */
        Instant now();
    }

    /**
     * Реализация источника времени на системных часах. Используется
     * в рабочем окружении, где реальное время допустимо.
     */
    static final class SystemClock implements ClockProvider {

        /**
         * Возвращает текущее системное время. Вызывается правилом окна
         * записи в рабочем окружении.
         */
        @Override
        public Instant now() {
            return Instant.now();
        }
    }

    /**
     * Фиксированные часы: всегда возвращают один и тот же момент.
     * Используются в тестах, где результат должен быть воспроизводимым.
     */
    static final class FixedClock implements ClockProvider {

        private final Instant fixed;

        /**
         * Запоминает фиксированный момент времени. Вызывается из main
         * при сборке движка для воспроизводимого вывода.
         */
        FixedClock(Instant fixed) {
            this.fixed = fixed;
        }

        /**
         * Возвращает зафиксированный момент времени. Вызывается правилом
         * окна записи вместо обращения к системным часам.
         */
        @Override
        public Instant now() {
            return fixed;
        }
    }

    /**
     * Простейшая реализация Portal: хранит, кто на какой курс записан,
     * в словаре «код курса -> список студентов». Заменяет настоящую базу.
     */
    static final class FakePortal implements Portal {

        private final java.util.Map<String, List<String>> enrolled =
                new java.util.HashMap<String, List<String>>();

        /**
         * Создаёт пустую базу. Вызывается из main при подготовке данных
         * для всех сценариев.
         */
        FakePortal() {
        }

        /**
         * Заранее занимает места указанными студентами: удобно для
         * демонстрации листа ожидания. Вызывается из main для курса
         * с вместимостью один.
         */
        void prefill(String courseCode, String... studentIds) {
            List<String> list = new ArrayList<String>();
            for (String id : studentIds) {
                list.add(id);
            }
            enrolled.put(courseCode, list);
        }

        /**
         * Возвращает число занятых мест. В настоящей системе здесь был бы
         * запрос к базе с подсчётом строк по коду курса. Вызывается
         * из CapacityRule.check(...).
         */
        @Override
        public int seatsTaken(String courseCode) {
            List<String> list = enrolled.get(courseCode);
            return list == null ? 0 : list.size();
        }

        /**
         * Имитирует проверку учебного плана: у студентов с идентификатором,
         * оканчивающимся на «0», плана нет. Вызывается
         * из DisciplinePlanRule.check(...).
         */
        @Override
        public boolean hasDisciplinePlan(String studentId) {
            return !studentId.endsWith("0");
        }

        /**
         * Добавляет студента в список зачисленных на курс. Вызывается
         * движком CourseHub.enroll(...) один раз при принятом решении Accepted.
         */
        @Override
        public void registerEnrollment(String studentId, String courseCode) {
            List<String> list = enrolled.get(courseCode);
            if (list == null) {
                list = new ArrayList<String>();
                enrolled.put(courseCode, list);
            }
            list.add(studentId);
        }
    }

    /**
     * Типизированный результат правила: правило возвращает не строку
     * и не boolean, а объект из закрытого набора вариантов. Так сервис
     * различает тонкие исходы (отказ и лист ожидания), а компилятор
     * проверяет полноту обработки.
     *
     * permits опущен: все подтипы объявлены в этом же файле, поэтому
     * компилятор находит их сам. Список нужно указывать вручную, только
     * если подтипы лежат в других файлах.
     */
    sealed interface RuleVerdict {

        /**
         * Возвращает человекочитаемое пояснение. Объявлено в интерфейсе,
         * чтобы сервис мог собрать отчёт, не зная конкретного варианта.
         * Вызывается из CourseHub.enroll(...) при формировании решения
         * и из RuleDecorator.check(...) при наблюдении за вердиктом.
         */
        String explain();
    }

    /**
     * Правило пройдено. Возвращается большинством листовых правил
     * и служит начальным значением в AllOfRule.check(...).
     */
    record Pass(String note) implements RuleVerdict {

        /**
         * Возвращает пояснение с префиксом "OK". Вызывается сервисом
         * и декоратором при печати вердикта.
         */
        @Override
        public String explain() {
            return "OK: " + note;
        }
    }

    /**
     * Правило не пройдено: отказ. У любого правила этот вердикт приводит
     * к решению Rejected.
     */
    record Fail(String reason) implements RuleVerdict {

        /**
         * Возвращает пояснение с префиксом "Отказ". Вызывается сервисом
         * при формировании решения Rejected и декоратором при печати.
         */
        @Override
        public String explain() {
            return "Отказ: " + reason;
        }
    }

    /**
     * Правило не может решить сейчас: нужен лист ожидания или ручное
     * решение. Именно этот вариант у любого правила превращается
     * в итоговое решение WaitListed.
     */
    record Defer(String reason) implements RuleVerdict {

        /**
         * Возвращает пояснение с префиксом "Отложено". Вызывается сервисом
         * при формировании решения WaitListed и декоратором при печати.
         */
        @Override
        public String explain() {
            return "Отложено: " + reason;
        }
    }

    /**
     * Открытый контракт правила зачисления.
     *
     * Здесь намеренно не sealed, в отличие от EnrollmentDecision: правила —
     * точка расширения системы, и их пишут другие команды в других пакетах
     * и сервисах. Закрытый список означал бы, что каждое новое правило
     * требует правки этого файла, то есть запрет на независимое развитие.
     * Контракт правил стабилен, а их количество растёт.
     *
     * Правила проектирования открытого контракта: держать его минимальным
     * (один метод плюс имя), не добавлять методы, нужные только одному
     * правилу, и передавать данные через контекст, а не новыми методами
     * интерфейса — иначе каждое новое правило сломает старые.
     */
    interface EnrollmentRule {

        /**
         * Возвращает имя правила для логов, отчётов и диагностики.
         * Вызывается сервисом в ruleNames(), при формировании решения
         * с причиной и в пояснении отказа у составных правил.
         */
        String name();

        /**
         * Проверяет кандидата по контексту. Метод не бросает исключений
         * для отрицательного результата — он возвращает Fail или Defer;
         * исключения оставлены для технических сбоев. Вызывается движком
         * CourseHub.enroll(...) и составными правилами AllOfRule и AnyOfRule.
         */
        RuleVerdict check(EnrollmentContext ctx);
    }

    /**
     * Снимок данных для правил: кандидат, курс, порт и часы.
     *
     * Это ключевой объект: правила получают всё нужное отсюда и больше
     * никуда не лезут. Благодаря этому правило легко тестировать
     * (подставляется готовый контекст), правило не зависит от CourseHub
     * и набор правил можно менять без правки контекста.
     */
    static final class EnrollmentContext {

        private final Student student;
        private final Course course;
        private final Portal portal;
        private final ClockProvider clock;

        /**
         * Сохраняет снимок данных. Вызывается движком CourseHub.enroll(...)
         * для каждой попытки зачисления и напрямую в main при проверке
         * вложенных деревьев правил.
         */
        EnrollmentContext(Student student, Course course, Portal portal, ClockProvider clock) {
            this.student = student;
            this.course = course;
            this.portal = portal;
            this.clock = clock;
        }

        /**
         * Возвращает кандидата. Вызывается листовыми правилами AgeRule,
         * PrerequisiteRule, DisciplinePlanRule, DuplicateEnrollmentRule,
         * LabAttendanceRule и новым PaymentPaidRule.
         */
        Student student() {
            return student;
        }

        /**
         * Возвращает курс. Вызывается PrerequisiteRule, CapacityRule,
         * DuplicateEnrollmentRule и LabAttendanceRule.
         */
        Course course() {
            return course;
        }

        /**
         * Возвращает порт наружу. Вызывается CapacityRule
         * и DisciplinePlanRule.
         */
        Portal portal() {
            return portal;
        }

        /**
         * Возвращает источник времени. Вызывается правилами, проверяющими
         * временные окна, и внедряется движком при создании контекста.
         */
        ClockProvider clock() {
            return clock;
        }
    }

    /**
     * Итоговое решение движка: тоже закрытый контракт, потому что набор
     * исходов фиксирован. permits перечисляет разрешённые подтипы явно,
     * так как они перечислены как вложенные типы с указанием на внешний класс.
     */
    sealed interface EnrollmentDecision
            permits CourseHubEngine.Accepted,
                    CourseHubEngine.Rejected,
                    CourseHubEngine.WaitListed {

        /**
         * Возвращает описание решения для журнала и вывода. Объявлено
         * в интерфейсе, чтобы клиент печатал решение, не зная подтипа.
         * Вызывается из CourseHub.enroll(...) при записи в аудит,
         * из исчерпывающего switch в main и из вспомогательного метода show(...).
         */
        String describe();
    }

    /**
     * Решение о зачислении. Создаётся движком, когда все правила
     * вернули Pass либо Defer не оказалось.
     */
    record Accepted(String studentId, String courseCode) implements EnrollmentDecision {

        /**
         * Возвращает описание «ЗАЧИСЛЕН». Вызывается сервисом при записи
         * в аудит, клиентом через switch и методом show(...).
         */
        @Override
        public String describe() {
            return "ЗАЧИСЛЕН " + studentId + " -> " + courseCode;
        }
    }

    /**
     * Решение об отказе с указанием правила и причины. Создаётся движком
     * при первом вердикте Fail с коротким замыканием цикла правил.
     */
    record Rejected(String studentId, String reason) implements EnrollmentDecision {

        /**
         * Возвращает описание «ОТКАЗ» с причиной. Вызывается сервисом
         * при записи в аудит, клиентом через switch и методом show(...).
         */
        @Override
        public String describe() {
            return "ОТКАЗ " + studentId + ": " + reason;
        }
    }

    /**
     * Решение о постановке в лист ожидания с номером позиции. Создаётся
     * движком, когда ни одно правило не отказало, но хотя бы одно
     * вернуло Defer.
     */
    record WaitListed(String studentId, int position) implements EnrollmentDecision {

        /**
         * Возвращает описание «ЛИСТ ОЖИДАНИЯ» с позицией. Вызывается сервисом
         * при записи в аудит, клиентом через switch и методом show(...).
         */
        @Override
        public String describe() {
            return "ЛИСТ ОЖИДАНИЯ " + studentId + ", позиция " + position;
        }
    }

    /**
     * Правило 1: минимальный возраст. Порог — данные конструктора,
     * поэтому отдельный класс на каждое значение не заводится.
     */
    static final class AgeRule implements EnrollmentRule {

        private final int minAge;

        /**
         * Задаёт минимальный возраст. Вызывается в main при сборке правил
         * движка и при создании вложенного дерева правил.
         */
        AgeRule(int minAge) {
            this.minAge = minAge;
        }

        /**
         * Возвращает имя "AGE_MIN". Используется в списке подключённых
         * правил, в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "AGE_MIN";
        }

        /**
         * Сравнивает возраст кандидата с порогом и возвращает Pass либо Fail
         * с указанием обоих чисел. Вызывается движком и составными
         * правилами AllOfRule и AnyOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            int age = ctx.student().age();
            if (age < minAge) {
                return new Fail("возраст " + age + " < " + minAge);
            }
            return new Pass("возраст " + age);
        }
    }

    /**
     * Правило 2: сданы ли пререквизиты курса. Не хранит состояния,
     * вся информация берётся из контекста.
     */
    static final class PrerequisiteRule implements EnrollmentRule {

        /**
         * Возвращает имя "PREREQUISITES". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "PREREQUISITES";
        }

        /**
         * Проверяет каждый пререквизит курса по сданным кодам кандидата
         * и возвращает Fail с первым несданным или Pass со списком
         * сданного. Вызывается движком и составными правилами.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            String[] prereqs = ctx.course().prerequisiteCodes();
            for (String p : prereqs) {
                if (!ctx.student().completed().contains(p)) {
                    return new Fail("не сдан пререквизит " + p);
                }
            }
            return new Pass("все пререквизиты сданы: " + ctx.student().completed());
        }
    }

    /**
     * Правило 3: вместимость. Если мест нет, возвращается не Fail,
     * а Defer: это не отказ, а постановка в лист ожидания. Так сервис
     * обходится без знания о конкретных правилах, потому что Defer
     * у любого правила означает WaitListed.
     */
    static final class CapacityRule implements EnrollmentRule {

        /**
         * Возвращает имя "CAPACITY". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "CAPACITY";
        }

        /**
         * Сравнивает занятые места с вместимостью и возвращает Pass
         * либо Defer с обеими цифрами. Вызывается движком напрямую,
         * а также вложенными правилами AllOfRule и AnyOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            int taken = ctx.portal().seatsTaken(ctx.course().code());
            int cap = ctx.course().capacity();
            if (taken < cap) {
                return new Pass("мест хватает (" + taken + "/" + cap + ")");
            }
            return new Defer("мест нет (" + taken + "/" + cap + ") — лист ожидания");
        }
    }

    /**
     * Правило 4: наличие индивидуального учебного плана. Данные
     * приходят из порта, поэтому правило ничего не знает о базе.
     */
    static final class DisciplinePlanRule implements EnrollmentRule {

        /**
         * Возвращает имя "DISCIPLINE_PLAN". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "DISCIPLINE_PLAN";
        }

        /**
         * Спрашивает у порта наличие плана и возвращает Pass либо Fail.
         * Вызывается движком напрямую и вложенным правилом AnyOfRule
         * в демонстрации рекурсивной композиции.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            if (ctx.portal().hasDisciplinePlan(ctx.student().id())) {
                return new Pass("учебный план утверждён");
            }
            return new Fail("нет утверждённого учебного плана");
        }
    }

    /**
     * Правило 5: повторная запись на тот же курс. Сделано через
     * объект с состоянием: правило помнит, что уже проверило, — видно,
     * что «чистота» правил не обязательна. Пара сравнивается по значению
     * «идентификатор@код курса», поэтому на разные курсы один студент
     * записаться может.
     */
    static final class DuplicateEnrollmentRule implements EnrollmentRule {

        private final List<String> seen = new ArrayList<String>();

        /**
         * Возвращает имя "DUPLICATE". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "DUPLICATE";
        }

        /**
         * Проверяет, не встречалась ли пара «студент@курс» ранее,
         * и запоминает новую. Возвращает Fail на повторе, иначе Pass.
         * Вызывается движком напрямую и вложенным правилом AllOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            String pair = ctx.student().id() + "@" + ctx.course().code();
            if (seen.contains(pair)) {
                return new Fail("повторная запись на курс " + ctx.course().code());
            }
            seen.add(pair);
            return new Pass("первая запись на курс " + ctx.course().code());
        }
    }

    /**
     * Правило 6 (бонус): лабораторные только для очного обучения.
     * Перечень кодов лабораторных передан как данные, поэтому новый
     * практикум не требует нового класса.
     */
    static final class LabAttendanceRule implements EnrollmentRule {

        private final String[] labCourseCodes;

        /**
         * Задаёт коды курсов с обязательным очным присутствием.
         * Вызывается в main при сборке правила «либо очный, либо свободный».
         */
        LabAttendanceRule(String... labCourseCodes) {
            this.labCourseCodes = labCourseCodes;
        }

        /**
         * Возвращает имя "LAB_ATTENDANCE". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "LAB_ATTENDANCE";
        }

        /**
         * Для перечисленных курсов требует признака очного обучения,
         * иначе возвращает Pass. Вызывается движком напрямую
         * и вложенным правилом AnyOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            for (String code : labCourseCodes) {
                if (code.equals(ctx.course().code()) && !ctx.student().resident()) {
                    return new Fail("лабораторные только для очного обучения");
                }
            }
            return new Pass("очное обучение не требуется");
        }
    }

    /**
     * Составное правило «все вложенные правила должны пройти».
     * Ключевая идея: AllOfRule само по себе является EnrollmentRule,
     * поэтому композиция рекурсивна — AllOf может содержать AnotherAllOf,
     * а тот AnyOf, и клиент не различает простое и составное правило.
     */
    static final class AllOfRule implements EnrollmentRule {

        private final String name;
        private final List<EnrollmentRule> children = new ArrayList<EnrollmentRule>();

        /**
         * Запоминает имя группы и её вложенные правила. Вызывается в main
         * для группы «места и уникальность», для демонстрации рекурсии
         * и при добавлении новых правил.
         */
        AllOfRule(String name, EnrollmentRule... rules) {
            this.name = name;
            for (EnrollmentRule r : rules) {
                children.add(r);
            }
        }

        /**
         * Возвращает заданное имя группы, а не имена вложенных правил.
         * Используется сервисом в ruleNames() и в причине решения.
         */
        @Override
        public String name() {
            return name;
        }

        /**
         * Копит худший вердикт вложенных правил: на Fail выходит сразу,
         * на Defer запоминает его и идёт дальше, иначе возвращает Pass.
         * Вызывается движком, декоратором и внешним AnyOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            RuleVerdict worst = new Pass("все вложенные правила пройдены");
            for (EnrollmentRule child : children) {
                RuleVerdict v = child.check(ctx);
                if (v instanceof Fail) {
                    return v;
                }
                if (v instanceof Defer) {
                    worst = v;
                }
            }
            return worst;
        }

        /**
         * Возвращает число вложенных правил. Используется клиентом при
         * проверке структуры составного правила.
         */
        int size() {
            return children.size();
        }
    }

    /**
     * Составное правило «достаточно, чтобы хотя бы одно вложенное правило
     * разрешило». Defer трактуется как разрешение с оговоркой: если Pass
     * нет, но есть Defer, возвращается Defer, потому что лист ожидания
     * лучше отказа.
     */
    static final class AnyOfRule implements EnrollmentRule {

        private final String name;
        private final List<EnrollmentRule> children = new ArrayList<EnrollmentRule>();

        /**
         * Запоминает имя группы и её вложенные правила. Вызывается в main
         * для группы «очный или свободный» и для вложенной группы
         * в демонстрации рекурсии.
         */
        AnyOfRule(String name, EnrollmentRule... rules) {
            this.name = name;
            for (EnrollmentRule r : rules) {
                children.add(r);
            }
        }

        /**
         * Возвращает заданное имя группы. Используется сервисом
         * в ruleNames() и в причине решения.
         */
        @Override
        public String name() {
            return name;
        }

        /**
         * Выходит сразу на первом Pass, запоминает первый Defer,
         * а если не нашлось ни того, ни другого — возвращает Fail
         * с перечнем имён вложенных правил. Вызывается движком,
         * декоратором и внешним AllOfRule.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            RuleVerdict defer = null;
            for (EnrollmentRule child : children) {
                RuleVerdict v = child.check(ctx);
                if (v instanceof Pass) {
                    return v;
                }
                if (v instanceof Defer && defer == null) {
                    defer = v;
                }
            }
            if (defer != null) {
                return defer;
            }
            return new Fail("ни одно из правил не выполнено: " + names());
        }

        /**
         * Собирает перечень имён вложенных правил через запятую. Вызывается
         * из check(...) этого же правила при формировании отказа.
         */
        private String names() {
            StringBuilder sb = new StringBuilder();
            for (EnrollmentRule r : children) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(r.name());
            }
            return sb.toString();
        }
    }

    /**
     * Декоратор правила: логирование и метрики вокруг любого правила.
     * Одна и та же обвязка работает и с листовым правилом, и с составным
     * (AllOfRule, AnyOfRule), потому что оба — EnrollmentRule.
     */
    static final class RuleDecorator implements EnrollmentRule {

        private final EnrollmentRule next;
        private final String label;

        /**
         * Оборачивает правило меткой для трассировки. Вызывается в main
         * при сборке списка правил движка и при проверке нового правила.
         */
        RuleDecorator(String label, EnrollmentRule next) {
            this.label = label;
            this.next = next;
        }

        /**
         * Возвращает имя оборачиваемого правила: декоратор прозрачен
         * и не меняет поведение, поэтому подменять имя нельзя.
         */
        @Override
        public String name() {
            return next.name();
        }

        /**
         * Печатает вход и выход оборачиваемого правила, затем возвращает
         * его вердикт без изменений — декоратор только наблюдает.
         * Вызывается движком CourseHub.enroll(...) при прогоне правил.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            System.out.println("      [" + label + "] -> " + next.name());
            RuleVerdict v = next.check(ctx);
            System.out.println("      [" + label + "] <- " + v.explain());
            return v;
        }
    }

    /**
     * Движок зачисления.
     *
     * В исходнике этого класса нет instanceof AgeRule, AllOfRule и прочих,
     * нет switch по типу правила и нет getClass().getSimpleName()
     * для выбора поведения: есть ровно два упоминания типов —
     * EnrollmentRule и EnrollmentDecision.
     *
     * Порядок работы: правила прогоняются по очереди; хоть одно вернуло
     * Fail — сразу Rejected, и это решение принимает правило, а не сервис;
     * иначе хоть одно вернуло Defer — WaitListed; иначе Accepted.
     * Логика уровня сервиса не знает, что означает конкретный отказ.
     */
    static final class CourseHub {

        private final List<EnrollmentRule> rules = new ArrayList<EnrollmentRule>();
        private final Portal portal;
        private final ClockProvider clock;
        private final List<String> audit = new ArrayList<String>();
        private int waitListCounter;

        /**
         * Зависимости передаются конструктором: сервис ничего не создаёт
         * сам, поэтому собирается в одной точке — в main. Копия списка
         * правил защищает сервис от внешних изменений.
         */
        CourseHub(List<EnrollmentRule> rules, Portal portal, ClockProvider clock) {
            this.rules.addAll(rules);
            this.portal = portal;
            this.clock = clock;
        }

        /**
         * Единственная точка входа движка. Создаёт контекст, прогоняет
         * правила с коротким замыканием на Fail, при Defer накапливает
         * первую причину, а при успехе единственный раз пишет запись
         * во внешнюю систему и возвращает решение, которое также кладёт
         * в журнал аудита. Вызывается из main для шести сценариев.
         */
        EnrollmentDecision enroll(Student student, Course course) {
            EnrollmentContext ctx = new EnrollmentContext(student, course, portal, clock);

            boolean sawDefer = false;
            String deferReason = null;

            for (EnrollmentRule rule : rules) {
                RuleVerdict verdict = rule.check(ctx);

                if (verdict instanceof Fail) {
                    Rejected decision = new Rejected(student.id(), rule.name() + " -> " + verdict.explain());
                    audit.add(decision.describe());
                    return decision;
                }
                if (verdict instanceof Defer) {
                    sawDefer = true;
                    if (deferReason == null) {
                        deferReason = rule.name() + " -> " + verdict.explain();
                    }
                }
            }

            if (sawDefer) {
                waitListCounter++;
                WaitListed decision = new WaitListed(student.id(), waitListCounter);
                audit.add(decision.describe() + " (" + deferReason + ")");
                return decision;
            }

            Accepted decision = new Accepted(student.id(), course.code());
            portal.registerEnrollment(student.id(), course.code());
            audit.add(decision.describe());
            return decision;
        }

        /**
         * Возвращает имена подключённых правил для диагностики. Вызывается
         * из main при выводе состава движка.
         */
        List<String> ruleNames() {
            List<String> names = new ArrayList<String>();
            for (EnrollmentRule r : rules) {
                names.add(r.name());
            }
            return names;
        }

        /**
         * Возвращает копию журнала решений. Каждое решение принято
         * в enroll(...), поэтому журнал виден после запуска.
         * Вызывается из main при выводе журнала.
         */
        List<String> auditTrail() {
            return new ArrayList<String>(audit);
        }
    }

    /**
     * Точка входа: собирает данные, порт, часы и правила (единственное
     * место, где правила создаются), прогоняет разных кандидатов
     * для демонстрации dynamic dispatch, печатает журнал решений,
     * проверяет рекурсивную композицию и исчерпывающую обработку
     * результата, добавляет новое правило и печатает записку
     * о выборе между наследованием и композицией и о гарантиях
     * sealed-границы.
     */
    public static void main(String[] args) {

        Course java = new Course("CRS-101", "Основы Java", 0,
                new String[]{}, 2);
        Course net = new Course("CRS-202", "Сетевые технологии", 6,
                new String[]{"CRS-101"}, 1);
        Course lab = new Course("CRS-303", "Лабораторный практикум", 0,
                new String[]{"CRS-101"}, 10);

        Student good = new Student("STU-1", 20, new SetOfCodes("CRS-101"), 30, true);
        Student young = new Student("STU-2", 16, new SetOfCodes(), 0, true);
        Student withoutPrereq = new Student("STU-3", 22, new SetOfCodes(), 30, true);
        Student last = new Student("STU-4", 24, new SetOfCodes("CRS-101"), 30, true);

        FakePortal portal = new FakePortal();
        portal.prefill("CRS-202", "STU-9");

        ClockProvider clock = new FixedClock(Instant.ofEpochMilli(1767225600000L));

        EnrollmentRule age = new AgeRule(18);
        EnrollmentRule prereq = new PrerequisiteRule();
        EnrollmentRule plan = new DisciplinePlanRule();
        EnrollmentRule duplicate = new DuplicateEnrollmentRule();

        EnrollmentRule capacityGroup = new AllOfRule("SEATS_AND_UNIQUE",
                new CapacityRule(), duplicate);
        EnrollmentRule residencyGroup = new AnyOfRule("RESIDENCY_OR_SEATS",
                new LabAttendanceRule("CRS-303"),
                new CapacityRule());

        List<EnrollmentRule> rules = new ArrayList<EnrollmentRule>();
        rules.add(new RuleDecorator("trace", age));
        rules.add(new RuleDecorator("trace", prereq));
        rules.add(new RuleDecorator("trace", plan));
        rules.add(new RuleDecorator("trace", capacityGroup));
        rules.add(new RuleDecorator("trace", residencyGroup));

        CourseHub hub = new CourseHub(rules, portal, clock);

        System.out.println("=== 1. ДВИЖОК СОБРАН ===");
        System.out.println("   подключённые правила: " + hub.ruleNames());
        System.out.println("   (CourseHub не знает, что это AgeRule или AllOfRule —");
        System.out.println("    он знает только интерфейс EnrollmentRule)");

        System.out.println();
        System.out.println("=== 2. ПРОГОН РАЗНЫХ КАНДИДАТОВ (dynamic dispatch) ===");
        System.out.println("   каждый вызов rule.check(ctx) уходит в свою реализацию:");

        System.out.println();
        System.out.println("   --- young (16 лет) -> отказ по возрасту ---");
        show(hub.enroll(young, java));

        System.out.println();
        System.out.println("   --- withoutPrereq (нет CRS-101) -> отказ по пререквизитам ---");
        show(hub.enroll(withoutPrereq, net));

        System.out.println();
        System.out.println("   --- good на java (мест 2) -> ЗАЧИСЛЕН, место занято ---");
        show(hub.enroll(good, java));

        System.out.println();
        System.out.println("   --- last на java (осталось 1 из 2 мест) -> ЗАЧИСЛЕН ---");
        show(hub.enroll(last, java));

        System.out.println();
        System.out.println("   --- good на net (мест 1, уже занято) -> ЛИСТ ОЖИДАНИЯ ---");
        show(hub.enroll(good, net));

        System.out.println();
        System.out.println("   --- good на практикум -> ЗАЧИСЛЕН (очный + есть место) ---");
        show(hub.enroll(good, lab));

        System.out.println();
        System.out.println("   --- good на java ПОВТОРНО -> отказ «повторная запись» ---");
        show(hub.enroll(good, java));

        System.out.println();
        System.out.println("=== 3. ЖУРНАЛ РЕШЕНИЙ ===");
        for (String line : hub.auditTrail()) {
            System.out.println("   " + line);
        }

        System.out.println();
        System.out.println("=== 4. КОМПОЗИЦИЯ РЕКУРСИВНА ===");
        EnrollmentRule deep = new AllOfRule("ALL",
                new AnyOfRule("INNER_ANY",
                        new AgeRule(18),
                        new DisciplinePlanRule()),
                new CapacityRule());
        Course openCourse = new Course("CRS-404", "Открытый курс", 0, new String[]{}, 100);
        Student zero = new Student("STU-0", 15, new SetOfCodes(), 0, true);

        System.out.println("   дерево: allOf{ anyOf{AGE_MIN, DISCIPLINE_PLAN}, CAPACITY }");
        RuleVerdict v1 = deep.check(new EnrollmentContext(young, openCourse, portal, clock));
        System.out.println("   young (16 лет, план есть):  " + v1.explain());
        RuleVerdict v2 = deep.check(new EnrollmentContext(zero, openCourse, portal, clock));
        System.out.println("   zero  (15 лет, плана нет):  " + v2.explain());
        System.out.println("   Один и тот же объект-правило, разные исходы: это и есть");
        System.out.println("   динамическая диспетчеризация внутри дерева.");
        System.out.println("   Клиенту неважно, одно это правило или дерево из 20.");
        System.out.println("   Тип правила: " + deep.getClass().getSimpleName()
                + ", тип вердикта: " + v1.getClass().getSimpleName()
                + " / " + v2.getClass().getSimpleName());

        System.out.println();
        System.out.println("=== 5. ИСЧЕРПЫВАЮЩАЯ ОБРАБОТКА РЕЗУЛЬТАТА (sealed) ===");
        EnrollmentDecision[] decisions = {
                new Accepted("STU-1", "CRS-101"),
                new Rejected("STU-2", "возраст"),
                new WaitListed("STU-3", 1)
        };
        for (EnrollmentDecision d : decisions) {
            String tag = switch (d) {
                case Accepted a -> "[A] " + a.describe();
                case Rejected r -> "[R] " + r.describe();
                case WaitListed w -> "[W] " + w.describe();
            };
            System.out.println("   " + tag);
        }
        System.out.println("   ВАЖНО: этот switch написан ОДИН раз для всех мест,");
        System.out.println("   где нужно показать результат. Никаких instanceof.");

        System.out.println();
        System.out.println("=== 6. ДОБАВЛЕНИЕ НОВОГО ПРАВИЛА (открытая стратегия) ===");
        System.out.println("   Добавим правило 'срок оплачен'. Это:");
        System.out.println("     1) +1 класс (implements EnrollmentRule);");
        System.out.println("     2) +1 строка rules.add(...) в сборке.");
        System.out.println("   Ни CourseHub, ни EnrollmentContext, ни другие правила");
        System.out.println("   не меняются. Сравните с sealed: там пришлось бы менять permits.");
        EnrollmentRule paymentCheck = new RuleDecorator("trace", new PaymentPaidRule());
        System.out.println();
        System.out.println("   проверка нового правила:");
        show(paymentCheck.check(new EnrollmentContext(good, java, portal, clock)));

        System.out.println();
        System.out.println("=== 7. ЗАПИСКА (обязательный пункт задания) ===");
        System.out.println();
        System.out.println("   A) ГДЕ НАСЛЕДОВАНИЕ УМЕСТНО (мы его почти не использовали):");
        System.out.println("      • Общий базовый класс для СУЩНОСТЕЙ с общим состоянием");
        System.out.println("        (BaseEntity: id, createdAt, equals — см. задание 5).");
        System.out.println("        Там отношение 'является' настоящее, и код общий.");
        System.out.println("      • Абстрактный класс с шаблонным методом, когда все наследники");
        System.out.println("        ОБЯЗАНЫ выполнять один и тот же алгоритм (задание 4).");
        System.out.println("      • Классы-потомки для «точки расширения» известной иерархии");
        System.out.println("        (например, Иерархия исключений).");
        System.out.println("      • sealed-иерархия фиксированных наборов (результат операции:");
        System.out.println("        Accepted/Rejected/WaitListed) — это КОНТРОЛИРУЕМОЕ наследование.");
        System.out.println();
        System.out.println("   B) ГДЕ ВЫБРАНА КОМПОЗИЦИЯ:");
        System.out.println("      • CourseHub + список правил (а не CourseHub extends BaseCourse):");
        System.out.println("        набор правил меняется в рантайме, это ДАННЫЕ, а не иерархия.");
        System.out.println("      • AllOfRule/AnyOfRule содержат другие правила (has-a),");
        System.out.println("        а не наследуют их (is-a).");
        System.out.println("      • RuleDecorator оборачивает правило, а не наследует его:");
        System.out.println("        декоратор применяется к ЛЮБОМУ правилу одинаково.");
        System.out.println("      • ClockProvider и Portal внедряются — сервис не знает,");
        System.out.println("        как устроены часы и база (инверсия зависимостей).");
        System.out.println();
        System.out.println("   C) ЧТО ДАЁТ SEALED-ГРАНИЦА:");
        System.out.println("      1) Компилятор знает ПОЛНЫЙ список подтипов.");
        System.out.println("      2) Switch без default проверяется на исчерпывающесть —");
        System.out.println("         забытый вариант = ошибка компиляции, не баг в проде.");
        System.out.println("      3) Нельзя случайно расширить иерархию извне (защита дизайна).");
        System.out.println("      4) Javadoc/API показывает закрытый набор вариантов.");
        System.out.println("      ЦЕНА: добавление варианта = правка permits + правка switch.");
        System.out.println("      Это приемлемо, когда набор исходов РЕАЛЬНО закрыт");
        System.out.println("      (результат операции), и неприемлемо, когда он открыт");
        System.out.println("      (правила — поэтому они на обычном интерфейсе).");
        System.out.println();
        System.out.println("   D) ПРАВИЛО ПРО ВЫБОР: если набор вариантов ИЗВЕСТЕН и меняется");
        System.out.println("      редко — sealed (выигрываем проверку компилятора).");
        System.out.println("      Если набор ОТКРЫТ и расширяется другими людьми — обычный");
        System.out.println("      интерфейс (выигрываем независимое развитие).");
    }

    /**
     * Вспомогательный метод для печати решения движка: использует
     * только метод интерфейса, поэтому одинаково подходит для всех
     * вариантов. Вызывается в main для каждого сценария зачисления.
     */
    private static void show(EnrollmentDecision d) {
        System.out.println("   РЕШЕНИЕ: " + d.describe());
    }

    /**
     * Вспомогательный метод для печати вердикта правила: использует
     * только метод интерфейса. Вызывается в main при проверке нового
     * правила и его декоратора.
     */
    private static void show(RuleVerdict v) {
        System.out.println("   ВЕРДИКТ: " + v.explain());
    }

    /**
     * Новое правило «срок оплачен», добавленное после того, как весь
     * остальной код был написан. Класс объявлен в конце файла, но
     * по правилам Java это неважно: классы не обязаны идти
     * в порядке использования.
     */
    static final class PaymentPaidRule implements EnrollmentRule {

        /**
         * Возвращает имя "PAYMENT_PAID". Используется в списке правил,
         * в отчёте и в причине решения.
         */
        @Override
        public String name() {
            return "PAYMENT_PAID";
        }

        /**
         * Проверяет наличие задолженности: в реальной системе здесь был бы
         * запрос к биллингу, а в примере задолженность имитируется по
         * последней цифре идентификатора. Возвращает Pass либо Fail.
         * Вызывается движком после добавления в список правил
         * и декоратором при печати трассировки.
         */
        @Override
        public RuleVerdict check(EnrollmentContext ctx) {
            boolean paid = !ctx.student().id().endsWith("9");
            if (paid) {
                return new Pass("задолженности нет");
            }
            return new Fail("есть задолженность за прошлый семестр");
        }
    }
}
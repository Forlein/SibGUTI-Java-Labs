import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Задание 11. «Равенство в иерархии».
 *
 * Задача файла: воспроизвести нарушение симметричности equals между Point
 * и ColoredPoint, предложить два исправления — строгое сравнение класса
 * и композицию цвета — и обосновать, почему класс-значение с final
 * проще расширяемой иерархии равенства.
 *
 * Контракт equals(Object), который нельзя нарушать: рефлексивность,
 * симметричность, транзитивность, согласованность и неравенство null.
 * Кроме того, равные объекты обязаны иметь одинаковый hashCode, иначе они
 * «исчезают» из HashSet и HashMap; обратное утверждение неверно.
 *
 * Запуск: javac 11_EqualityInHierarchy.java
 *         java -cp . EqualityInHierarchy
 *
 * Требования к среде: JDK 8+.
 */
class EqualityInHierarchy {

    /**
     * Исходная, сломанная иерархия: точка на плоскости, чей equals
     * сравнивает координаты через instanceof, то есть принимает подклассы.
     * Именно эта терпимость к подклассам и порождает нарушение симметричности
     * вместе с equals подкласса ColoredPoint.
     */
    static class Point {

        private final int x;
        private final int y;

        /**
         * Сохраняет координаты точки. Вызывается в main при создании
         * экземпляров Point и как super-вызов из конструктора ColoredPoint.
         */
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Возвращает координату X. Используется клиентом при печати
         * и в тестах равенства.
         */
        int getX() {
            return x;
        }

        /**
         * Возвращает координату Y. Используется клиентом при печати
         * и в тестах равенства.
         */
        int getY() {
            return y;
        }

        /**
         * Сравнение «по содержимому части»: подкласс тоже является Point,
         * поэтому instanceof его не отвергает. Из-за этого point.equals(colored)
         * и colored.equals(point) дают разные ответы. Вызывается клиентом,
         * коллекциями HashSet и HashMap при поиске, а также из
         * equals подкласса через super.equals(other).
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Point)) {
                return false;
            }
            Point other = (Point) o;
            return x == other.x && y == other.y;
        }

        /**
         * Возвращает комбинацию координат; 31 — простое число-множитель,
         * улучшающее распределение, переполнение int здесь безопасно.
         * Вызывается HashSet и HashMap, а также equals/hashCode подкласса.
         */
        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        /**
         * Возвращает краткое представление точки. Вызывается из main
         * при печати состава экспериментальных данных.
         */
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    /**
     * Цвет в виде отдельного типа. Выделен в отдельный класс, чтобы показать,
     * что именно его добавление к наследуемому типу и портило равенство.
     */
    static class Color {

        private final int r;
        private final int g;
        private final int b;

        /**
         * Сохраняет компоненты цвета. Вызывается в main при создании
         * цветов для всех экспериментов с равенством.
         */
        Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        /**
         * Сравнивает все три компоненты через instanceof. Корректен сам по себе,
         * потому что у Color нет подклассов. Вызывается из equals
         * ColoredPoint, StrictColoredPoint и ComposedColoredPoint.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Color)) {
                return false;
            }
            Color other = (Color) o;
            return r == other.r && g == other.g && b == other.b;
        }

        /**
         * Возвращает комбинацию компонентов цвета. Вызывается коллекциями
         * и equals/hashCode цветных точек, содержащих этот цвет.
         */
        @Override
        public int hashCode() {
            return 31 * (31 * r + g) + b;
        }

        /**
         * Возвращает цвет в виде rgb(...). Вызывается из toString()
         * цветных точек при печати экспериментальных данных.
         */
        @Override
        public String toString() {
            return "rgb(" + r + "," + g + "," + b + ")";
        }
    }

    /**
     * Цветная точка: наследует Point и добавляет цвет. equals написан
     * «по правилам» — проверяет наличие цвета через instanceof, — и именно
     * поэтому симметричность ломается: Point не знает о цвете, а подкласс требует.
     */
    static class ColoredPoint extends Point {

        private final Color color;

        /**
         * Сохраняет координаты через super и добавляет цвет.
         * Вызывается в main при создании двух экземпляров для проверки
         * симметричности и последствий для коллекций.
         */
        ColoredPoint(int x, int y, Color color) {
            super(x, y);
            this.color = color;
        }

        /**
         * Возвращает цвет точки. Используется клиентом при печати
         * и описании экспериментальных данных.
         */
        Color getColor() {
            return color;
        }

        /**
         * Сравнение «по всему содержимому»: координаты и цвет. Для Point цвет
         * неизвестен, поэтому plain.equals(colored) и colored.equals(plain)
         * дают разные ответы. Вызывается клиентом напрямую, а также
         * HashSet и HashMap при поиске и добавлении ключей.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ColoredPoint)) {
                return false;
            }
            ColoredPoint other = (ColoredPoint) o;
            return super.equals(other) && color.equals(other.color);
        }

        /**
         * Хэш подкласса обязан включать хэш базового, иначе нарушится контракт
         * «равен => одинаковый хэш». Вызывается коллекциями и используется
         * в equals/hashCode строгих и композитных вариантов как образец.
         */
        @Override
        public int hashCode() {
            return 31 * super.hashCode() + color.hashCode();
        }

        /**
         * Возвращает координаты и цвет. Вызывается из main при печати
         * состава экспериментальных данных.
         */
        @Override
        public String toString() {
            return super.toString() + color;
        }
    }

    /**
     * Исправление 1: строгое сравнение класса вместо instanceof.
     * Два объекта равны, только если они одного и того же класса и имеют
     * одинаковые поля. Класс объявлен final, поэтому наследование,
     * которое и было источником проблемы, исключено.
     *
     * Плата за решение: новое поле в подклассе пришлось бы учесть в equals,
     * а базовый класс не может узнать о нём, поэтому расширять такую иерархию
     * либо нельзя, либо приходится полностью переопределять equals и hashCode.
     * Именно так по спецификации ведут себя record в Java 16+.
     */
    static final class StrictPoint {

        private final int x;
        private final int y;

        /**
         * Сохраняет координаты строгой точки. Вызывается в main,
         * а также из конструктора StrictColoredPoint, где служит
         * составной частью.
         */
        StrictPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Сравнивает класс через getClass(): разные классы всегда дают false,
         * поэтому симметричность и транзитивность держатся автоматически.
         * Вызывается клиентом, а также equals и hashCode StrictColoredPoint.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StrictPoint other = (StrictPoint) o;
            return x == other.x && y == other.y;
        }

        /**
         * Возвращает комбинацию координат. Вызывается HashSet в main
         * и equals/hashCode StrictColoredPoint.
         */
        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        /**
         * Возвращает представление с префиксом SP. Вызывается из main
         * при печати результатов.
         */
        @Override
        public String toString() {
            return "SP" + x + "," + y;
        }
    }

    /**
     * Цветная точка в строгом смысле: вместо наследования содержит
     * StrictPoint, поэтому каждый тип сравнивается только сам с собой.
     * Класс final — расширить его нельзя.
     */
    static final class StrictColoredPoint {

        private final StrictPoint point;
        private final Color color;

        /**
         * Создаёт внутреннюю строгую точку и сохраняет цвет.
         * Вызывается в main при создании двух экземпляров для проверки
         * строгого равенства.
         */
        StrictColoredPoint(int x, int y, Color color) {
            this.point = new StrictPoint(x, y);
            this.color = color;
        }

        /**
         * Сравнивает класс через getClass() и затем обе составные части.
         * Вызывается клиентом при проверке равенства.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StrictColoredPoint other = (StrictColoredPoint) o;
            return point.equals(other.point) && color.equals(other.color);
        }

        /**
         * Комбинирует хэши составных частей, сохраняя контракт
         * «равен => одинаковый хэш». Используется при работе с коллекциями.
         */
        @Override
        public int hashCode() {
            return 31 * point.hashCode() + color.hashCode();
        }

        /**
         * Возвращает представление вида точка:цвет. Вызывается из main
         * при печати результатов.
         */
        @Override
        public String toString() {
            return point + ":" + color;
        }
    }

    /**
     * Исправление 2: композиция цвета вместо наследования.
     * ComposedColoredPoint не является подтипом Point, а лишь ссылается на него,
     * поэтому сравнивать их между собой не требуется и новое поле не может
     * просочиться в чужой equals.
     */
    static final class ComposedColoredPoint {

        private final Point point;
        private final Color color;

        /**
         * Сохраняет готовые точку и цвет, не копируя их внутрь.
         * Вызывается в main при создании трёх экземпляров
         * для проверки равенства.
         */
        ComposedColoredPoint(Point point, Color color) {
            this.point = point;
            this.color = color;
        }

        /**
         * Возвращает вложенную точку. Используется клиентом, когда нужно
         * получить координаты без цвета.
         */
        Point getPoint() {
            return point;
        }

        /**
         * Возвращает вложенный цвет. Используется клиентом, когда нужно
         * получить цвет без координат.
         */
        Color getColor() {
            return color;
        }

        /**
         * Сравнивает класс через getClass() и затем обе составные части,
         * чьи equals уже корректны. Вызывается клиентом напрямую
         * и потенциально коллекциями.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComposedColoredPoint other = (ComposedColoredPoint) o;
            return point.equals(other.point) && color.equals(other.color);
        }

        /**
         * Комбинирует хэши составных частей. Используется при работе
         * с HashSet и HashMap.
         */
        @Override
        public int hashCode() {
            return 31 * point.hashCode() + color.hashCode();
        }

        /**
         * Возвращает представление вида точка:цвет. Вызывается из main
         * при печати результатов.
         */
        @Override
        public String toString() {
            return point + ":" + color;
        }
    }

    /**
     * Третье решение — класс-значение на примере Money.
     *
     * Класс-значение имеет все поля private final, не имеет наследников,
     * создаётся полностью собранным конструктором, а equals, hashCode
     * и toString определены по всем полям.
     *
     * Почему такая иерархия проще:
     * 1) равным объектам не нужно наследовать равенство, поэтому правило
     *    простое и проверяемое: тот же класс и те же значения полей;
     * 2) случай «подкласс со своим equals и забытым полем» становится
     *    невозможен из-за final на классе;
     * 3) симметричность становится структурной, а не «аккуратной»;
     * 4) в JDK такие примеры есть — Integer, Instant, а в Java 16+ record
     *    является синтаксическим сахаром для того же самого;
     * 5) final-поля дают безопасную публикацию по JMM: видимые значения
     *    гарантированно корректны даже без синхронизации.
     *
     * Цена: если сущности нужны в иерархии, класс-значение не годится,
     * тогда выбирают композицию или ограниченное наследование.
     */
    static final class Money {

        private final long amount;
        private final String currency;

        /**
         * Сохраняет сумму в минорных единицах и валюту. Вызывается в main
         * при создании трёх экземпляров для проверки равенства и согласованности
         * с hashCode.
         */
        Money(long amount, String currency) {
            this.amount = amount;
            this.currency = currency;
        }

        /**
         * Сравнивает класс через getClass(), затем сумму и валюту.
         * Вызывается клиентом и коллекциями при работе с Money.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Money other = (Money) o;
            return amount == other.amount && currency.equals(other.currency);
        }

        /**
         * Комбинирует хэши суммы и валюты. Вызывается HashSet и HashMap
         * и сравнивается с результатом equals в main.
         */
        @Override
        public int hashCode() {
            return 31 * Long.valueOf(amount).hashCode() + currency.hashCode();
        }

        /**
         * Возвращает сумму в человекочитаемом виде с разделением целой
         * и минорной части. Вызывается из main при печати результатов.
         */
        @Override
        public String toString() {
            return amount / 100 + "." + (amount % 100) + " " + currency;
        }
    }

    /**
     * Точка входа: демонстрирует нарушение симметричности между Point
     * и ColoredPoint, проверяет строгое равенство, композицию цвета и класс-значение,
     * а также показывает последствия для HashSet и HashMap и типичные ошибки
     * реализации equals.
     */
    public static void main(String[] args) {

        System.out.println("=== 1. ВОСПРОИЗВЕДЕНИЕ НАРУШЕНИЯ СИММЕТРИЧНОСТИ ===");
        Point plain = new Point(1, 2);
        ColoredPoint colored = new ColoredPoint(1, 2, new Color(255, 0, 0));
        ColoredPoint colored2 = new ColoredPoint(1, 2, new Color(0, 0, 255));

        System.out.println("   plain   = " + plain);
        System.out.println("   colored = " + colored);
        System.out.println("   colored2= " + colored2 + "  (другой цвет)");
        System.out.println();
        System.out.println("   plain.equals(colored)  = " + plain.equals(colored)
                + "   <- Point не знает про цвет");
        System.out.println("   colored.equals(plain)  = " + colored.equals(plain)
                + "   <- ColoredPoint требует и цвет");
        System.out.println("   ВЫВОД: два вызова дали РАЗНЫЕ ответы на ОДИН И ТОТ ЖЕ вопрос.");
        System.out.println("   Симметричность нарушена. Это баг, а не особенность.");
        System.out.println("   (это же ломает транзитивность: если добавить объект,");
        System.out.println("    равный обоим, цепочка рассыпается)");

        System.out.println();
        System.out.println("=== 2. СИММЕТРИЯ РАБОТАЕТ, А ХЭШ — НЕТ ===");
        System.out.println("   plain.hashCode()   = " + plain.hashCode());
        System.out.println("   colored.hashCode() = " + colored.hashCode());
        System.out.println("   Разные -> нарушения контракта «равен => одинаковый хэш» нет,");
        System.out.println("   но фактически объекты НЕ равны, так что это законно.");
        System.out.println("   А вот если бы мы написали equals «по координатам» в ColoredPoint");
        System.out.println("   и забыли про цвет — тогда получился бы «равен, но хэш разный»,");
        System.out.println("   и объект «исчезал» бы из HashSet. Проверим на шаге 4.");

        System.out.println();
        System.out.println("=== 3. ИСПРАВЛЕНИЕ 1: STRICT CLASS EQUALITY ===");
        StrictPoint sp1 = new StrictPoint(1, 2);
        StrictPoint sp2 = new StrictPoint(1, 2);
        StrictPoint sp3 = new StrictPoint(3, 4);
        StrictColoredPoint sc1 = new StrictColoredPoint(1, 2, new Color(255, 0, 0));
        StrictColoredPoint sc2 = new StrictColoredPoint(1, 2, new Color(255, 0, 0));
        System.out.println("   sp1.equals(sp2) = " + sp1.equals(sp2) + "  (ожидаем true)");
        System.out.println("   sp1.equals(sp3) = " + sp1.equals(sp3) + "  (ожидаем false)");
        System.out.println("   sc1.equals(sc2) = " + sc1.equals(sc2) + "  (ожидаем true)");
        System.out.println("   Симметричность держится АВТОМАТИЧЕСКИ: разные классы ->");
        System.out.println("   getClass() != getClass() -> обе стороны возвращают false.");
        System.out.println("   Цена: класс final, расширить нельзя.");

        System.out.println();
        System.out.println("=== 4. ИСПРАВЛЕНИЕ 2: КОМПОЗИЦИЯ ЦВЕТА ===");
        ComposedColoredPoint c1 = new ComposedColoredPoint(new Point(1, 2), new Color(255, 0, 0));
        ComposedColoredPoint c2 = new ComposedColoredPoint(new Point(1, 2), new Color(255, 0, 0));
        ComposedColoredPoint c3 = new ComposedColoredPoint(new Point(1, 2), new Color(0, 0, 255));
        System.out.println("   c1.equals(c2) = " + c1.equals(c2) + "  (ожидаем true)");
        System.out.println("   c1.equals(c3) = " + c1.equals(c3) + "  (ожидаем false)");
        System.out.println("   Наследования нет: ComposedColoredPoint НЕ является Point.");
        System.out.println("   Сравнивать точку с цветной точкой даже не имеет смысла.");
        System.out.println("   Составные части сравниваются своими (корректными) equals.");

        System.out.println();
        System.out.println("=== 5. VALUE CLASS: Money ===");
        Money m1 = new Money(199900, "RUB");
        Money m2 = new Money(199900, "RUB");
        Money m3 = new Money(199900, "USD");
        System.out.println("   m1 = " + m1 + ", m2 = " + m2 + ", m3 = " + m3);
        System.out.println("   m1.equals(m2) = " + m1.equals(m2) + "  (ожидаем true)");
        System.out.println("   m1.equals(m3) = " + m1.equals(m3) + "  (ожидаем false)");
        System.out.println("   equals и hashCode согласованы: "
                + (m1.hashCode() == m2.hashCode()) + " / " + (m1.hashCode() == m3.hashCode()));

        System.out.println();
        System.out.println("=== 6. ПОСЛЕДСТВИЯ ДЛЯ HashSet/HashMap ===");
        Set<Point> points = new HashSet<Point>();
        points.add(plain);
        points.add(colored);
        System.out.println("   colored.equals(plain) = " + colored.equals(plain)
                + ", а plain.equals(colored) = " + plain.equals(colored));
        System.out.println("   set.contains(new Point(1,2))        = " + points.contains(new Point(1, 2)));
        System.out.println("   set.contains(new ColoredPoint(1,2,красный)) = "
                + points.contains(new ColoredPoint(1, 2, new Color(255, 0, 0))));
        System.out.println("   Размер множества: " + points.size()
                + " (две ссылки, которые colored.equals(plain) считает равными,");
        System.out.println("    но у которых разные hashCode — коллекция им не доверяет)");

        Map<Point, String> labels = new HashMap<Point, String>();
        labels.put(plain, "точка");
        labels.put(colored, "цветная точка");
        System.out.println("   labels.get(plain)  = " + labels.get(plain));
        System.out.println("   labels.get(new Point(1,2)) = " + labels.get(new Point(1, 2))
                + "  <- нашлось по координатам, хотя цветные разные");

        System.out.println();
        System.out.println("   А теперь проверим С ИСПРАВЛЕННЫМИ типами:");
        Set<StrictPoint> good = new HashSet<StrictPoint>();
        good.add(sp1);
        good.add(new StrictPoint(1, 2));
        good.add(sp3);
        System.out.println("   размер множества строгих точек = " + good.size()
                + "  (ожидаем 2: 1 + 1 разных)");

        System.out.println();
        System.out.println("=== 7. ВОПРОСЫ, КОТОРЫЕ СТОИТ ЗАДАТЬ СЕБЕ ===");
        System.out.println("   • Поле Double: Double.equals(0.1 + 0.2) == false.");
        System.out.println("     Для денег и точных вычислений используют BigDecimal,");
        System.out.println("     у которого equals тоже строгий (сравнивает scale!).");
        System.out.println("   • Поле-коллекция в equals: сравниваются ССЫЛКИ на списки,");
        System.out.println("     а не содержимое -> используйте List.equals или Arrays.equals.");
        System.out.println("   • Если класс НЕизменяемый, equals может быть final.");
        System.out.println("   • Наследуемый equals — всегда подозрителен: спросите себя,");
        System.out.println("     может ли подкласс иметь те же значения полей.");
    }
}
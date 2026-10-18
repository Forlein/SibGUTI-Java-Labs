package tasks;

import java.util.Objects;

/**
 * Задание 11. Равенство в иерархии.
 * Воспроизведите нарушение симметричности между Point и ColoredPoint.
 * Предложите два исправления: strict class equality и композиция цвета.
 * Защитите, почему final value class проще расширяемой equality-иерархии.
 */
public class Task11EqualityInHierarchy {

    // === 1. Воспроизведение нарушения симметричности (instanceof в иерархии) ===
    static class Point {
        protected final int x;
        protected final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point that)) return false;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static class ColoredPoint extends Point {
        private final String color;

        ColoredPoint(int x, int y, String color) {
            super(x, y);
            this.color = Objects.requireNonNull(color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ColoredPoint that)) return false;
            return super.equals(that) && color.equals(that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), color);
        }
    }

    // === 2. Исправление А: Strict class equality (getClass() == o.getClass()) ===
    static class StrictPoint {
        protected final int x;
        protected final int y;

        StrictPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StrictPoint that = (StrictPoint) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static class StrictColoredPoint extends StrictPoint {
        private final String color;

        StrictColoredPoint(int x, int y, String color) {
            super(x, y);
            this.color = Objects.requireNonNull(color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StrictColoredPoint that = (StrictColoredPoint) o;
            return super.equals(that) && color.equals(that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), color);
        }
    }

    // === 3. Исправление Б: Композиция цвета и final value class ===
    static final class ValuePoint {
        private final int x;
        private final int y;

        ValuePoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValuePoint that = (ValuePoint) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static final class ComposedColoredPoint {
        private final ValuePoint point;
        private final String color;

        ComposedColoredPoint(int x, int y, String color) {
            this.point = new ValuePoint(x, y);
            this.color = Objects.requireNonNull(color);
        }

        public ValuePoint asPoint() {
            return point;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComposedColoredPoint that = (ComposedColoredPoint) o;
            return point.equals(that.point) && color.equals(that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, color);
        }
    }

    public static void main(String[] args) {
        Point p = new Point(1, 2);
        ColoredPoint cp = new ColoredPoint(1, 2, "RED");

        System.out.println("=== 1. Нарушение симметричности между Point и ColoredPoint ===");
        System.out.println("  p.equals(cp) = " + p.equals(cp) + " (Point игнорирует цвет)");
        System.out.println("  cp.equals(p) = " + cp.equals(p) + " (ColoredPoint требует совпадения цвета)");

        System.out.println("\n=== 2. Исправление 1: Strict class equality (getClass() == o.getClass()) ===");
        StrictPoint sp = new StrictPoint(1, 2);
        StrictColoredPoint scp = new StrictColoredPoint(1, 2, "RED");
        System.out.println("  sp.equals(scp) = " + sp.equals(scp));
        System.out.println("  scp.equals(sp) = " + scp.equals(sp) + " (симметричность восстановлена)");

        System.out.println("\n=== 3. Исправление 2: Композиция цвета (ComposedColoredPoint) ===");
        ValuePoint vp = new ValuePoint(1, 2);
        ComposedColoredPoint ccp1 = new ComposedColoredPoint(1, 2, "RED");
        ComposedColoredPoint ccp2 = new ComposedColoredPoint(1, 2, "RED");
        System.out.println("  ccp1.equals(ccp2)         = " + ccp1.equals(ccp2));
        System.out.println("  ccp1.asPoint().equals(vp) = " + ccp1.asPoint().equals(vp));

        System.out.println("\n=== 4. Почему final value class проще расширяемой equality-иерархии ===");
        System.out.println("  - Фундаментальная теорема ООП (Effective Java, Item 10): невозможно расширить инстанциируемый класс,");
        System.out.println("    добавив значимое поле (value component), и одновременно сохранить симметричность, транзитивность и LSP.");
        System.out.println("  - В final value class (или Java record) нет подклассов с дополнительными полями, поэтому");
        System.out.println("    рефлексивность, симметричность, транзитивность и согласованность с hashCode выполняются тривиально.");
    }
}

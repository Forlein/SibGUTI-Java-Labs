class Point {
    private final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; // Строгое сравнение
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }
}

// ColoredPoint БОЛЬШЕ НЕ НАСЛЕДУЕТ Point. Он содержит его внутри.
class ColoredPoint {
    private final Point point;
    private final String color;

    public ColoredPoint(int x, int y, String color) {
        this.point = new Point(x, y);
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColoredPoint that = (ColoredPoint) o;
        return point.equals(that.point) && color.equals(that.color);
    }
}

public class Main_2 {
    public static void main(String[] args) {
        Point p = new Point(1, 2);
        ColoredPoint cp = new ColoredPoint(1, 2, "Red");

        System.out.println("p.equals(cp): " + p.equals(cp));  // false (разные классы)
        System.out.println("cp.equals(p): " + cp.equals(p));  // false (разные классы)
        System.out.println("Симметрия соблюдена!");
    }
}
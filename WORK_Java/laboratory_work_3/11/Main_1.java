class Point {
    protected final int x, y;
    public Point(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
}

class ColoredPoint extends Point {
    private final String color;
    public ColoredPoint(int x, int y, String color) {
        super(x, y);
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Требуем, чтобы объект был именно ColoredPoint
        if (!(o instanceof ColoredPoint)) return false;
        ColoredPoint cp = (ColoredPoint) o;
        return super.equals(o) && color.equals(cp.color);
    }
}

public class Main_1 {
    public static void main(String[] args) {
        Point p = new Point(1, 2);
        ColoredPoint cp = new ColoredPoint(1, 2, "Red");

        System.out.println("p.equals(cp): " + p.equals(cp));  // true (Point не видит цвет)
        System.out.println("cp.equals(p): " + cp.equals(p));  // false (ColoredPoint требует цвет)
        System.out.println("Симметрия нарушена!");
    }
}
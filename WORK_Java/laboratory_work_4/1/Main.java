/*
Задание 1. Прочитать stack trace
Получите исключение в цепочке из четырёх методов. 
Найдите тип, сообщение, место возникновения и путь вызовов. 
Исправьте причину, а не последнюю строку stack trace.
*/
public class Main {
    static int d(String s) { return 10 / Integer.parseInt(s); }
    static int c(String s) { return d(s); }
    static int b(String s) { return c(s); }
    static int a(String s) { return b(s); }

    public static void main(String[] args) {
        try {
            a("0");
        } catch (ArithmeticException e) {
            System.out.println("type    = " + e.getClass().getSimpleName());
            System.out.println("message = " + e.getMessage());
            StackTraceElement[] st = e.getStackTrace();
            System.out.println("place   = " + st[0]);
            System.out.print("path    = ");
            for (int i = 0; i < 4; i++) System.out.print(st[i].getMethodName() + (i < 3 ? " -> " : "\n"));
        }
        System.out.println("fixed   = " + a("2"));
    }
}

/*
## Уровень II. Управление ошибками
Задание 4. `try-catch-finally
Предскажите порядок выполнения для return и throw внутри 
`try`, `catch` и `finally`. Не возвращайте значение из 
`finally`. Объясните, как такой return способен скрыть 
исключение.
*/
public class Main {

    static int success() {
        try { System.out.println("try"); return 1; }
        finally { System.out.println("finally"); }
    }

    static int caught() {
        try { throw new RuntimeException("boom"); }
        catch (RuntimeException e) { System.out.println("catch"); return 2; }
        finally { System.out.println("finally"); }
    }

    static int badFinally() {
        try { throw new RuntimeException("boom"); }
        finally { System.out.println("finally"); return 99; }   // ПЛОХО: глотает исключение
    }

    public static void main(String[] args) {
        System.out.println("--- success ---");
        System.out.println("result = " + success());

        System.out.println("--- caught ---");
        System.out.println("result = " + caught());

        System.out.println("--- badFinally ---");
        System.out.println("result = " + badFinally());   // исключение НЕ вылетит
    }
}
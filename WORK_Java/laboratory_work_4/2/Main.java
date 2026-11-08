/*
Задание 2. Checked или unchecked
Распределите десять ситуаций: неверный аргумент метода, 
отсутствующий файл, нарушение состояния, сетевой отказ, 
ошибка конфигурации, недостаток памяти. Для каждой выберите 
checked exception, unchecked exception, другой результат или невозможность 
восстановления и объясните решение.
*/
public class Main {

    static void checkedExample() throws java.io.IOException {
        throw new java.io.IOException("нет файла");
    }

    static void uncheckedExample() {
        throw new IllegalArgumentException("плохой аргумент");
    }

    static void errorExample() {
        throw new OutOfMemoryError("нет памяти");
    }

    public static void main(String[] args) {
        try { uncheckedExample(); }
        catch (RuntimeException e) { System.out.println("1 неверный аргумент -> unchecked: " + e.getMessage()); }

        try { checkedExample(); }
        catch (java.io.IOException e) { System.out.println("2 нет файла -> checked: " + e.getMessage()); }

        try { throw new IllegalStateException("курс уже завершён"); }
        catch (RuntimeException e) { System.out.println("3 состояние -> unchecked: " + e.getMessage()); }

        try { throw new java.io.IOException("сеть недоступна"); }
        catch (java.io.IOException e) { System.out.println("4 сеть -> checked: " + e.getMessage()); }

        System.out.println("5 конфигурация -> другой результат (fail fast), не исключение в рантайме");

        try { errorExample(); }
        catch (OutOfMemoryError e) { System.out.println("6 память -> Error (невосстановимо): " + e.getMessage()); }
    }
}

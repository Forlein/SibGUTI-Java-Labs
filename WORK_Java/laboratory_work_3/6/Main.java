class Course { } 

interface Socket {
    String doWork(Course course);
}

// Родительский класс (нормальный прибор)
class Iron implements Socket {
    @Override
    public String doWork(Course course) {
        return "1 (Правильный метод)";
    }
}

// Класс-ловушка (наследник)
class IronBroken extends Iron {
    
    // ТУТ ОШИБКА: Нет @Override. И параметр Object вместо Course.
    // Компилятор думает, что это просто новый метод.
    public String doWork(Object course) {
        return "ОШИБКА: сработал не тот метод!";
    }
}

public class Main {
    public static void main(String[] args) {
        Course c = new Course();
        IronBroken broken = new IronBroken();

        // Вызов 1: Передаем Course. Компилятор выбирает doWork(Course).
        // Сработает метод из родителя (Iron), потому что в наследнике его нет.
        System.out.println(broken.doWork(c)); 
        // ВЫВОД: 1 (Правильный метод)

        // Вызов 2: Передаем Object. Компилятор выбирает doWork(Object).
        // Сработает метод-ловушка из наследника.
        System.out.println(broken.doWork((Object) c)); 
        // ВЫВОД: ОШИБКА: сработал не тот метод!
    }
}
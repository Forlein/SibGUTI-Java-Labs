class Course { } // Просто "что-то", что мы форматируем

// Это РОЗЕТКА. Правило: "Кто хочет работать от сети, должен уметь делать format"
interface Socket {
    String doWork(Course course);
}

// Прибор 1: Утюг (Печатает 1)
class Iron implements Socket {
    @Override
    public String doWork(Course course) {
        return "1";
    }
}

// Прибор 2: Телефон (Печатает 2)
class Phone implements Socket {
    @Override
    public String doWork(Course course) {
        return "2";
    }
}

// Прибор 3: Чайник (Печатает 3)
class Kettle implements Socket {
    @Override
    public String doWork(Course course) {
        return "3";
    }
}

public class Main {
    public static void main(String[] args) {
        Course c = new Course();

        // Массив "розеток", в которые воткнуты разные приборы
        Socket[] devices = {
            new Iron(),
            new Phone(),
            new Kettle()
        };

        // Просто включаем каждый по очереди. Без if-ов!
        for (Socket device : devices) {
            System.out.println(device.doWork(c));
        }
    }
}
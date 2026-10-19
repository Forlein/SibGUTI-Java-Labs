interface CourseReader {
    String findById(int id);
}

interface CourseWriter extends CourseReader {
    void save(String course);
}

class DatabaseRepository implements CourseWriter {
    public void save(String course) {
        System.out.println("Сохранено в БД: " + course);
    }

    public String findById(int id) {
        return "Курс из БД";
    }
}

class ReadOnlyRepository implements CourseReader {
    public String findById(int id) {
        return "Курс из кэша";
    }
}

public class Main {
    public static void main(String[] args) {
        CourseReader reader = new ReadOnlyRepository();
        System.out.println(reader.findById(1));

        CourseWriter writer = new DatabaseRepository();
        writer.save("Java");
        System.out.println(writer.findById(1));
    }
}
// 1. Интерфейс — это контракт (Только методы, без полей)
interface Identifiable {
    long getId(); 
}

// 2. Абстрактный класс — это каркас (Есть поля и общий код)
abstract class BaseEntity implements Identifiable {
    protected long id; // Общее поле для всех сущностей

    public BaseEntity(long id) {
        this.id = id;
    }

    // Уже готовая реализация метода из интерфейса
    @Override
    public long getId() {
        return id;
    }
}

// 3. Обычный класс — готовая сущность (Наследует каркас)
class Student extends BaseEntity {
    private String name;

    public Student(long id, String name) {
        super(id); // Передаем id в BaseEntity
        this.name = name;
    }
    
    public String getName() { return name; }
}

// 4. Еще один интерфейс — чистое поведение (Форматтер)
interface Formatter {
    String format(Student student);
}

class ShortFormatter implements Formatter {
    @Override
    public String format(Student s) {
        return "Студент: " + s.getName();
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("--- ДЕМОНСТРАЦИЯ КОДА ---\n");

        // Создаем обычный класс (Student), который наследует абстрактный (BaseEntity)
        Student student = new Student(101, "Иван");
        
        // Используем интерфейс (Formatter) для форматирования
        Formatter formatter = new ShortFormatter();
        
        System.out.println("ID студента (из BaseEntity): " + student.getId());
        System.out.println("Форматирование (из Formatter): " + formatter.format(student));

        System.out.println("\n=======================================================");
        System.out.println("              ОБЪЯСНЕНИЕ (ЧИТАЙ ТУТ)");
        System.out.println("=======================================================\n");

        System.out.println("1. INTERFACE (Интерфейс) — это ПРАВИЛО (контракт).");
        System.out.println("   Пример: Identifiable, Formatter.");
        System.out.println("   Суть: Говорит 'ЧТО' нужно уметь делать (getId, format), но не говорит 'КАК'.");
        System.out.println("   Зачем: Чтобы класс мог иметь много таких правил сразу (в Java нельзя наследовать два класса, а интерфейсов — сколько угодно).\n");

        System.out.println("2. ABSTRACT CLASS (Абстрактный класс) — это КАРКАС.");
        System.out.println("   Пример: BaseEntity.");
        System.out.println("   Суть: Умеет хранить данные (поля, например 'id') и содержит готовый код, но может иметь 'дырки' (абстрактные методы).");
        System.out.println("   Зачем: Чтобы не дублировать код. Написал один раз в BaseEntity — и все наследники (Student) этим пользуются.\n");

        System.out.println("3. ОБЫЧНЫЙ CLASS (Обычный класс) — это ГОТОВАЯ ВЕЩЬ.");
        System.out.println("   Пример: Student.");
        System.out.println("   Суть: От него можно создать объект через 'new'. Он наследует каркас (BaseEntity) и выполняет правила (Identifiable).\n");

        System.out.println("--- ГЛАВНОЕ ПРАВИЛО ВЫБОРА ---");
        System.out.println("• Нужно только поведение (методы)? -> Делай INTERFACE.");
        System.out.println("• Нужны общие поля и готовый код? -> Делай ABSTRACT CLASS.");
        System.out.println("• Нужна конкретная сущность с данными? -> Делай обычный CLASS.\n");
    }
}
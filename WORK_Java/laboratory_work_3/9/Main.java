class Student { int age; Student(int age) { this.age = age; } }
class Course { }

interface EnrollmentPolicy {
    boolean isAllowed(Student s, Course c);
}

// Правило 1: По возрасту (18+)
class AgePolicy implements EnrollmentPolicy {
    public boolean isAllowed(Student s, Course c) { return s.age >= 18; }
}

// Правило 2: По пререквизитам (допустим, всегда true для простоты)
class PrereqPolicy implements EnrollmentPolicy {
    public boolean isAllowed(Student s, Course c) { return true; }
}

// Правило 3: По вместимости (допустим, всегда true)
class CapacityPolicy implements EnrollmentPolicy {
    public boolean isAllowed(Student s, Course c) { return true; }
}

// Клиент
class EnrollmentService {
    private EnrollmentPolicy policy;
    
    EnrollmentService(EnrollmentPolicy policy) { this.policy = policy; }

    void enroll(Student s, Course c) {
        if (policy.isAllowed(s, c)) System.out.println("Записан!");
        else System.out.println("Отказ!");
    }
}

public class Main {
    public static void main(String[] args) {
        Student kid = new Student(16);
        Student adult = new Student(20);
        Course c = new Course();

        // Меняем стратегию на лету
        EnrollmentService service = new EnrollmentService(new AgePolicy());
        service.enroll(kid, c);   // Отказ
        service.enroll(adult, c); // Записан
    }
}
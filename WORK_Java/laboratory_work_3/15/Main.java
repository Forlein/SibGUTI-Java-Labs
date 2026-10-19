sealed interface RuleResult permits Ok, Fail {}
record Ok() implements RuleResult {}
record Fail(String reason) implements RuleResult {}

interface Rule {
    RuleResult check(String student);
}

class AgeRule implements Rule {
    public RuleResult check(String student) {
        return student.equals("Вася") ? new Fail("Слишком молод") : new Ok();
    }
}

class PaymentRule implements Rule {
    public RuleResult check(String student) {
        return student.equals("Иван") ? new Fail("Нет денег") : new Ok();
    }
}

class AllOf implements Rule {
    private final Rule[] rules;
    AllOf(Rule... rules) { this.rules = rules; }
    
    public RuleResult check(String student) {
        for (Rule r : rules) {
            RuleResult res = r.check(student);
            if (res instanceof Fail f) return f;
        }
        return new Ok();
    }
}

class AnyOf implements Rule {
    private final Rule[] rules;
    AnyOf(Rule... rules) { this.rules = rules; }
    
    public RuleResult check(String student) {
        for (Rule r : rules) {
            if (r.check(student) instanceof Ok) return new Ok();
        }
        return new Fail("Ни одно правило не пройдено");
    }
}

class EnrollmentService {
    private final Rule rule;
    EnrollmentService(Rule rule) { this.rule = rule; }

    void enroll(String student) {
        RuleResult result = rule.check(student);
        switch (result) {
            case Ok o -> System.out.println("УСПЕХ: " + student + " записан!");
            case Fail f -> System.out.println("ОТКАЗ: " + student + " (" + f.reason() + ")");
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Rule age = new AgeRule();
        Rule payment = new PaymentRule();
        
        Rule strictRule = new AllOf(age, payment);
        Rule lenientRule = new AnyOf(age, payment);
        
        System.out.println("--- AllOf ---");
        new EnrollmentService(strictRule).enroll("Вася");
        new EnrollmentService(strictRule).enroll("Иван");
        new EnrollmentService(strictRule).enroll("Петя");

        System.out.println("\n--- AnyOf ---");
        new EnrollmentService(lenientRule).enroll("Вася");
        new EnrollmentService(lenientRule).enroll("Иван");
    }
}
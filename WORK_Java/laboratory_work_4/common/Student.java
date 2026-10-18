package common;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Общая модель студента, используемая в заданиях 5, 9, 12, 14, 15.
 */
public class Student extends BaseEntity {
    private final String fullName;
    private final int age;
    private final Set<String> completedCourses;
    private final int completedCredits;
    private final boolean resident;
    private final boolean benefit;

    public Student(String id, String fullName, int age) {
        this(id, fullName, age, Collections.emptySet(), 0, true, false);
    }

    public Student(String id, String fullName, int age, boolean benefit) {
        this(id, fullName, age, Collections.emptySet(), 0, true, benefit);
    }

    public Student(String id, String fullName, int age, Set<String> completedCourses,
                   int completedCredits, boolean resident) {
        this(id, fullName, age, completedCourses, completedCredits, resident, false);
    }

    public Student(String id, String fullName, int age, Set<String> completedCourses,
                   int completedCredits, boolean resident, boolean benefit) {
        super(id, Instant.EPOCH);
        this.fullName = fullName;
        this.age = age;
        this.completedCourses = new LinkedHashSet<>(
                completedCourses != null ? completedCourses : Collections.emptySet());
        this.completedCredits = completedCredits;
        this.resident = resident;
        this.benefit = benefit;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public Set<String> getCompletedCourses() {
        return Collections.unmodifiableSet(completedCourses);
    }

    public int getCompletedCredits() {
        return completedCredits;
    }

    public boolean isResident() {
        return resident;
    }

    public boolean hasBenefit() {
        return benefit;
    }

    @Override
    public String toString() {
        return "Student{id='" + id() + "', name='" + fullName + "', age=" + age
                + ", completed=" + completedCourses + ", resident=" + resident + "}";
    }
}

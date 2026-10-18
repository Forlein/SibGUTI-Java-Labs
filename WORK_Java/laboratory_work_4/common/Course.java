package common;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Общая модель курса, используемая в заданиях 1, 3, 5, 8, 9, 12, 15.
 */
public class Course extends BaseEntity {
    private final String code;
    private final String title;
    private final int credits;
    private final boolean mandatory;
    private final Set<String> prerequisites;
    private final int capacity;

    public Course(String code, String title) {
        this(code, title, 4, true, Collections.emptySet(), 30);
    }

    public Course(String code, String title, int credits) {
        this(code, title, credits, true, Collections.emptySet(), 30);
    }

    public Course(String code, String title, int credits, boolean mandatory) {
        this(code, title, credits, mandatory, Collections.emptySet(), 30);
    }

    public Course(String code, String title, int credits, Set<String> prerequisites, int capacity) {
        this(code, title, credits, true, prerequisites, capacity);
    }

    public Course(String code, String title, int credits, boolean mandatory,
                  Set<String> prerequisites, int capacity) {
        super(code, Instant.EPOCH);
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.mandatory = mandatory;
        this.prerequisites = new LinkedHashSet<>(prerequisites != null ? prerequisites : Collections.emptySet());
        this.capacity = capacity;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public Set<String> getPrerequisites() {
        return Collections.unmodifiableSet(prerequisites);
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return code + " '" + title + "' (" + credits + " кред., "
                + (mandatory ? "обязат." : "по выбору") + ", мест: " + capacity + ")";
    }
}

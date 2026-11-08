public class Course {
    private int completedHours;
    private final int totalHours;

    public Course(int totalHours) {
        if (totalHours < 0) throw new IllegalArgumentException("totalHours < 0");
        this.totalHours = totalHours;
    }

    public void completeHours(int hours) {
        if (hours < 0) throw new IllegalArgumentException("hours < 0");
        if (completedHours + hours > totalHours) throw new IllegalStateException("too many hours");
        completedHours += hours;
    }

    public int getCompletedHours() { return completedHours; }
}

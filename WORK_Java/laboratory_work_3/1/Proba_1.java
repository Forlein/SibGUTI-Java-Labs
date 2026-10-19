class Course{}

    interface CourseFormatter {
        String format(Course course);
    }

    class CompactCourseFormatter implements CourseFormatter {
        @Override
        public String format(Course course) {
            return "1";
        }
    }

    class DetailedCourseFormatter implements CourseFormatter {
        @Override
        public String format(Course course) {
            return "2";
        }
    }

public class Proba_1 {

    public static void main(String[] args){

        Course c = new Course();

        CourseFormatter f = new CompactCourseFormatter();
        System.out.print(f.format(c));

        f = new DetailedCourseFormatter();
        System.out.print(f.format(c));

    }

}

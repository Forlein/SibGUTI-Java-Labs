package task_12_repository;

public class task_12_repository {

    public static void main(String[] args) {
        /* Задача 12: generic repository — in-memory реализация без утечки внутренней коллекции. */
        repository<Integer, course> course_repository = new in_memory_repository<>(course::get_id);

        course_repository.save(new course(1, "CS101", "Программирование"));
        course_repository.save(new course(2, "MATH12", "Матанализ"));
        course_repository.save(new course(1, "CS101X", "Обновлённый курс"));

        System.out.println("find_by_id(1): " + course_repository.find_by_id(1));
        System.out.println("find_all: " + course_repository.find_all());

        course_repository.delete_by_id(2);
        System.out.println("После delete_by_id(2): " + course_repository.find_all());

        try {
            course_repository.save(null);
        } catch (Exception null_save_error) {
            System.out.println("save(null): " + null_save_error.getClass().getSimpleName());
        }

        try {
            course_repository.find_all().add(new course(9, "HACK", "Внешнее изменение"));
        } catch (Exception immutable_error) {
            System.out.println("find_all нельзя менять: " + immutable_error.getClass().getSimpleName());
        }
    }
}

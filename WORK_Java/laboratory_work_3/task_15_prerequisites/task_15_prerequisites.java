package task_15_prerequisites;

public class task_15_prerequisites {

    public static void main(String[] args) {
        /* Задача 15: граф зависимостей курсов — допустимый порядок изучения или диагностический путь цикла. */
        graph course_graph = new graph();
        course_graph.add_vertex("CS101");
        course_graph.add_vertex("CS102");
        course_graph.add_vertex("ALGO");
        course_graph.add_vertex("DB");
        course_graph.add_edge("CS101", "CS102");
        course_graph.add_edge("CS102", "ALGO");
        course_graph.add_edge("CS101", "DB");
        System.out.println("Порядок изучения: " + course_graph.topological_order());

        graph cyclic_graph = new graph();
        cyclic_graph.add_edge("A", "B");
        cyclic_graph.add_edge("B", "C");
        cyclic_graph.add_edge("C", "A");
        System.out.println("Граф с циклом: " + cyclic_graph.topological_order());

        graph missing_graph = new graph();
        missing_graph.add_edge("X", "Y");
        System.out.println("Зависимости X: " + missing_graph.dependencies_of("X"));
        System.out.println("Зависимости отсутствующей Z: " + missing_graph.dependencies_of("Z"));
    }
}

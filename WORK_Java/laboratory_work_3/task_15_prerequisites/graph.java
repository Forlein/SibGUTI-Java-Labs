package task_15_prerequisites;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class graph {
    private final Map<String, Set<String>> adjacency_map = new TreeMap<>();

    public void add_vertex(String vertex_name) {
        /* Добавляет вершину без зависимостей; повторный вызов ничего не меняет. */
        adjacency_map.putIfAbsent(vertex_name, new TreeSet<>());
    }

    public void add_edge(String from_vertex, String to_vertex) {
        /* Ребро from -> to значит «to зависит от from»; отсутствующие вершины создаются автоматически. */
        adjacency_map.computeIfAbsent(from_vertex, vertex -> new TreeSet<>()).add(to_vertex);
        adjacency_map.computeIfAbsent(to_vertex, vertex -> new TreeSet<>());
    }

    public Set<String> dependencies_of(String vertex_name) {
        /* Возвращает копию зависимостей; для отсутствующей вершины — пустой set без исключения. */
        return new TreeSet<>(adjacency_map.getOrDefault(vertex_name, Set.of()));
    }

    public List<String> topological_order() {
        /* Топосортировка алгоритмом Кана, O(V+E). TreeMap/TreeSet дают детерминированный порядок. */
        Map<String, Integer> in_degree = new TreeMap<>();
        for (String vertex_name : adjacency_map.keySet()) {
            in_degree.put(vertex_name, 0);
        }
        for (Set<String> target_vertices : adjacency_map.values()) {
            for (String target_vertex : target_vertices) {
                in_degree.merge(target_vertex, 1, Integer::sum);
            }
        }

        Deque<String> ready_queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> degree_entry : in_degree.entrySet()) {
            if (degree_entry.getValue() == 0) {
                ready_queue.add(degree_entry.getKey());
            }
        }

        List<String> order_list = new ArrayList<>();
        while (!ready_queue.isEmpty()) {
            String current_vertex = ready_queue.poll();
            order_list.add(current_vertex);
            for (String next_vertex : adjacency_map.getOrDefault(current_vertex, Set.of())) {
                if (in_degree.merge(next_vertex, -1, Integer::sum) == 0) {
                    ready_queue.add(next_vertex);
                }
            }
        }

        if (order_list.size() != adjacency_map.size()) {
            return List.of("ЦИКЛ: " + String.join(" -> ", find_cycle()));
        }
        return order_list;
    }

    private List<String> find_cycle() {
        /* Ищет цикл в глубину и возвращает путь из вершин цикла для диагностики. */
        Set<String> visited_vertices = new TreeSet<>();
        List<String> current_path = new ArrayList<>();
        for (String vertex_name : adjacency_map.keySet()) {
            List<String> cycle_path = depth_first_search(vertex_name, visited_vertices, current_path);
            if (cycle_path != null) {
                return cycle_path;
            }
        }
        return new ArrayList<>();
    }

    private List<String> depth_first_search(String vertex_name, Set<String> visited_vertices, List<String> current_path) {
        /* DFS: вершина уже в пути (серая) означает цикл — возвращаем срез пути от неё до текущей. */
        int cycle_start_index = current_path.indexOf(vertex_name);
        if (cycle_start_index >= 0) {
            List<String> cycle_path = new ArrayList<>(current_path.subList(cycle_start_index, current_path.size()));
            cycle_path.add(vertex_name);
            return cycle_path;
        }
        if (!visited_vertices.add(vertex_name)) {
            return null;
        }
        current_path.add(vertex_name);
        for (String next_vertex : adjacency_map.getOrDefault(vertex_name, Set.of())) {
            List<String> cycle_path = depth_first_search(next_vertex, visited_vertices, current_path);
            if (cycle_path != null) {
                return cycle_path;
            }
        }
        current_path.remove(current_path.size() - 1);
        return null;
    }
}

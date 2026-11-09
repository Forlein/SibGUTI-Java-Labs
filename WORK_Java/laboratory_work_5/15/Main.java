/*Курсы образуют ориентированный граф зависимостей. 
Постройте generic-модель графа на map/set, найдите допустимый 
порядок изучения или цикл, выдайте диагностический путь цикла. 
Результат детерминирован при одинаковом вводе. Защитите выбор коллекций, 
сложности и поведение при отсутствующей вершине.*/

import java.util.*;

public class Main {
    static final class Graph<T> {
        private final Map<T, Set<T>> adj = new TreeMap<>();   // вершина -> соседи

        void addVertex(T v) { adj.computeIfAbsent(v, k -> new TreeSet<>()); }
        void addEdge(T from, T to) { addVertex(from); addVertex(to); adj.get(from).add(to); }

        Set<T> vertices() { return adj.keySet(); }
        Set<T> neighbors(T v) { return adj.getOrDefault(v, Set.of()); }
    }

    static <T> List<T> topoOrCycle(Graph<T> g) {
        Map<T, Integer> indeg = new TreeMap<>();
        for (T v : g.vertices()) indeg.put(v, 0);
        for (T v : g.vertices()) for (T n : g.neighbors(v)) indeg.merge(n, 1, Integer::sum);

        Deque<T> q = new ArrayDeque<>();
        for (var e : indeg.entrySet()) if (e.getValue() == 0) q.add(e.getKey());

        List<T> order = new ArrayList<>();
        while (!q.isEmpty()) {
            T v = q.poll();
            order.add(v);
            for (T n : g.neighbors(v)) if (indeg.merge(n, -1, Integer::sum) == 0) q.add(n);
        }
        if (order.size() == indeg.size()) return order;

        Set<T> visited = new HashSet<>();
        for (T v : g.vertices())
            if (indeg.get(v) > 0) {
                List<T> c = dfsCycle(g, v, visited, new LinkedHashSet<>());
                if (c != null) return c;
            }
        return List.of();
    }

    static <T> List<T> dfsCycle(Graph<T> g, T v, Set<T> visited, LinkedHashSet<T> path) {
        if (path.contains(v)) {
            List<T> p = new ArrayList<>(path);
            return new ArrayList<>(p.subList(p.indexOf(v), p.size()));
        }
        if (!visited.add(v)) return null;
        path.add(v);
        for (T n : g.neighbors(v)) {
            List<T> c = dfsCycle(g, n, visited, path);
            if (c != null) return c;
        }
        path.remove(v);
        return null;
    }

    public static void main(String[] args) {
        Graph<String> g = new Graph<>();
        g.addEdge("A", "B"); g.addEdge("B", "C"); g.addEdge("A", "C");
        System.out.println("order=" + topoOrCycle(g));      // [A, B, C]

        Graph<String> cyc = new Graph<>();
        cyc.addEdge("A", "B"); cyc.addEdge("B", "C"); cyc.addEdge("C", "A");
        System.out.println("cycle=" + topoOrCycle(cyc));    // [A, B, C]

        Graph<String> m = new Graph<>();
        m.addVertex("X");
        System.out.println("neighbors X=" + m.neighbors("X"));      // []
        System.out.println("neighbors ?=" + m.neighbors("???"));    // [] — без NPE
    }
}
/*Спроектируйте Repository<ID,T> с save, findById, findAll, 
deleteById. In-memory реализация не отдаёт наружу изменяемую 
внутреннюю коллекцию, не принимает null, определяет семантику 
повторного save.*/

import java.util.*;

public class Main {
    interface Repository<ID, T> {
        T save(ID id, T entity);
        Optional<T> findById(ID id);
        List<T> findAll();
        boolean deleteById(ID id);
    }

    static final class InMemoryRepo<ID, T> implements Repository<ID, T> {
        private final Map<ID, T> store = new HashMap<>();

        @Override public T save(ID id, T entity) {
            Objects.requireNonNull(id);
            Objects.requireNonNull(entity);
            return store.put(id, entity);      // upsert: возвращает предыдущее значение или null
        }

        @Override public Optional<T> findById(ID id) {
            Objects.requireNonNull(id);
            return Optional.ofNullable(store.get(id));
        }

        @Override public List<T> findAll() {
            return List.copyOf(store.values()); // неизменяемый снимок
        }

        @Override public boolean deleteById(ID id) {
            Objects.requireNonNull(id);
            return store.remove(id) != null;
        }
    }

    public static void main(String[] args) {
        Repository<String, String> repo = new InMemoryRepo<>();

        System.out.println(repo.save("C1", "Course A"));   // null — вставка
        System.out.println(repo.save("C1", "Course A2"));  // "Course A" — обновление
        System.out.println(repo.findById("C1"));           // Optional[Course A2]
        System.out.println(repo.findAll());                // [Course A2]
        System.out.println(repo.deleteById("C1"));         // true
        System.out.println(repo.findAll());                // []

        try { repo.findAll().add("x"); }
        catch (UnsupportedOperationException e) { System.out.println("findAll immutable"); }

        try { repo.save(null, "x"); }
        catch (NullPointerException e) { System.out.println("null id rejected"); }
    }
}
package task_12_repository;

import java.util.List;

public interface repository<ID, T> {

    void save(T entity);

    T find_by_id(ID entity_id);

    List<T> find_all();

    void delete_by_id(ID entity_id);
}

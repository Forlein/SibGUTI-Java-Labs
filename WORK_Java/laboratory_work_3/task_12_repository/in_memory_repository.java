package task_12_repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class in_memory_repository<ID, T> implements repository<ID, T> {
    private final Map<ID, T> entity_store = new LinkedHashMap<>();
    private final Function<T, ID> id_extractor;

    public in_memory_repository(Function<T, ID> id_extractor) {
        /* Извлекатель ID даёт гибкость: хранилище не требует общего базового класса у сущностей. */
        this.id_extractor = id_extractor;
    }

    @Override
    public void save(T entity) {
        /* Сохраняет сущность; null запрещён, повторный save по тому же ID перезаписывает (upsert). */
        if (entity == null) {
            throw new IllegalArgumentException("entity == null");
        }
        ID entity_id = id_extractor.apply(entity);
        if (entity_id == null) {
            throw new IllegalArgumentException("entity_id == null");
        }
        entity_store.put(entity_id, entity);
    }

    @Override
    public T find_by_id(ID entity_id) {
        /* Ищет по ID; при отсутствии бросает исключение, чтобы не возвращать null. */
        T found_entity = entity_store.get(entity_id);
        if (found_entity == null) {
            throw new NoSuchElementException("Нет сущности с id=" + entity_id);
        }
        return found_entity;
    }

    @Override
    public List<T> find_all() {
        /* Возвращает неизменяемую копию, чтобы снаружи нельзя было менять хранилище. */
        return Collections.unmodifiableList(new ArrayList<>(entity_store.values()));
    }

    @Override
    public void delete_by_id(ID entity_id) {
        /* Удаляет сущность по ID; отсутствие ID не считается ошибкой. */
        entity_store.remove(entity_id);
    }
}

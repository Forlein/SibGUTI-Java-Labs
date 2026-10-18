package common;

import java.time.Instant;
import java.util.Objects;

/**
 * Базовый абстрактный класс сущности с общим состоянием и равенством по id (Задание 5).
 */
public abstract class BaseEntity implements Identifiable {
    private final String id;
    private final Instant createdAt;
    private Instant updatedAt;

    protected BaseEntity(String id, Instant createdAt) {
        if (id == null || id.trim().isEmpty()) {
            this.id = "GEN-" + Integer.toHexString(System.identityHashCode(this));
        } else {
            this.id = id;
        }
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = this.createdAt;
    }

    @Override
    public final String id() {
        return id;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final Instant getUpdatedAt() {
        return updatedAt;
    }

    public final void touch(Instant when) {
        this.updatedAt = Objects.requireNonNull(when, "when must not be null");
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', createdAt=" + createdAt + "}";
    }
}

package common;

import java.time.Instant;

/**
 * Общий интерфейс источника времени (Задания 5, 15).
 */
public interface ClockProvider {
    Instant now();
}

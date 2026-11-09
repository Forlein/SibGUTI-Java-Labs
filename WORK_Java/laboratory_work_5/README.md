# Лабораторная работа 5. Коллекции и обобщения в Java

15 заданий, каждое — отдельная папка `1`–`15` с одним `Main.java` и методом `main`.
Все задания собираются независимо и используют только стандартную библиотеку JDK.

Темы работы: коллекции (`List`, `Set`, `Map`, `Queue`, `Deque`), их внутреннее
устройство и сложность операций, `equals`/`hashCode`, generic-типы и generic-методы,
wildcards и PECS, ограничения параметров типа.

Сборка и запуск отдельного задания (достаточно JDK 21):

```
cd WORK_Java/laboratory_work_5
cd 1
javac -encoding UTF-8 -d out Main.java
java -Dstdout.encoding=UTF-8 -cp out Main
```

## Задания

| Папка | Задание |
|---|---|
| `1` | `ArrayList` и `LinkedList`: вставка, удаление и доступ по индексу, сравнение скорости `get` |
| `2` | Уникальность сущности по коду: `List` + `Set`, корректные `equals`/`hashCode` |
| `3` | `Map` «ID → объект»: `get`, `containsKey`, `putIfAbsent`, `computeIfAbsent` |
| `4` | Очередь FIFO: `add`/`offer`, `remove`/`poll`, `element`/`peek` и поведение на пустой очереди |
| `5` | `ConcurrentModificationException`: удаление в `for-each`, через `Iterator.remove` и `removeIf` |
| `6` | Generic `Result<T>` вместо raw types: фабрики `ok`/`err`, ошибки на этапе компиляции |
| `7` | Generic-методы: `firstOrThrow`, `swap`, `indexBy` с `Function` |
| `8` | Generic `max` для `Comparable`: ограничение параметра типа и сравнение value object |
| `9` | Wildcards и PECS: producer `? extends` и consumer `? super` |
| `10` | Ключи в `HashSet`/`HashMap`: `equals`/`hashCode` и опасность изменяемых полей |
| `11` | Выбор коллекции под задачу и сравнительная таблица по операциям и сложности |
| `12` | Generic `Repository<ID, T>`: in-memory хранилище, `Optional`, проверки `null`, неизменяемый `findAll` |
| `13` | Связь «многие ко многим»: `Map<Tag, Set<CourseId>>`, пересечение и очистка пустых наборов |
| `14` | Generic `LruCache<K, V>` на `LinkedHashMap` с `accessOrder` |
| `15` | Граф зависимостей: топологическая сортировка и поиск цикла, generic-граф |

Требуется JDK 21.

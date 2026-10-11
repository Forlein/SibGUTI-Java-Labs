# Лабораторная работа 3. Коллекции Java, дженерики, выбор структур данных

15 заданий, каждое — отдельная папка, имя которой совпадает с именем пакета.
Внутри каждой папки:

- `*.java` — исходники; главный класс с методом `main` назван так же, как папка;
- `run.bat` — сборка и запуск задания в один клик (Windows).

Каждое задание собирается независимо. Общий класс `course` продублирован в нужных
пакетах, поэтому при общей сборке конфликтов имён не возникает.

Сборка и запуск одного задания — достаточно JDK 21, Maven не нужен:

```
javac -encoding UTF-8 task_01_list_order\*.java
java -Dstdout.encoding=UTF-8 -cp . task_01_list_order.task_01_list_order
```

Или просто двойной клик по `run.bat` внутри папки задания.

Сборка всех заданий сразу (из корня `laboratory_work_3`):

```
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java).FullName
```

Готовый отчёт по работе — `Отчет_ЛР3_Java.docx`.

## Задания

| Пакет (папка) | Задание |
|---|---|
| `task_01_list_order` | List и порядок: вставка, удаление, дубликаты, ArrayList против LinkedList |
| `task_02_set_uniqueness` | Set и уникальность: дедупликация с сохранением порядка |
| `task_03_map_index` | Map и индекс: доступ по ключу, запрет перезаписи |
| `task_04_waitlist_queue` | Очередь ожидания: FIFO на Queue |
| `task_05_safe_removal` | Безопасное удаление при обходе коллекции |
| `task_06_result_type` | Первый generic-тип: контейнер result<T> |
| `task_07_generic_methods` | Generic-методы над списками |
| `task_08_comparable_max` | Ограничение типа: max для Comparable |
| `task_09_pecs` | Принцип PECS на практике |
| `task_10_key_contract` | Контракт ключа в hashCode/equals |
| `task_11_load_choice` | Выбор коллекции под нагрузку |
| `task_12_repository` | Generic-репозиторий |
| `task_13_inverted_index` | Обратный индекс |
| `task_14_lru_cache` | Ограниченный LRU-кэш |
| `task_15_prerequisites` | Планировщик предпосылок (топологическая сортировка) |

Требуется JDK 21.

# WORK_Java

Лабораторные работы по курсу «Программирование на Java» (СибГУТИ, 3–4 курс).

Каждая лабораторная лежит в своей папке и не зависит от остальных:

| Лабораторная | Папка | Содержание | Сборка |
|---|---|---|---|
| № 1. Полиморфизм и проектирование | [`laboratory_work_1/`](laboratory_work_1) | 15 заданий, каждое в отдельном `.java`-файле с `main` | `javac` + `java`, без сборщика |
| № 2. Исключения в Java | [`laboratory_work_2/`](laboratory_work_2) | 15 папок-заданий, в каждой `main`, `test` и `data` | Maven, корневой `pom.xml` |
| № 3. Коллекции и дженерики | [`laboratory_work_3/`](laboratory_work_3) | 15 папок-заданий, в каждой пакет, `main` и `run.bat` | `javac` + `java`, без сборщика |
| № 4. Контракты, полиморфизм и проектирование | [`laboratory_work_4/`](WORK_Java/laboratory_work_4) | Папка чистых задач `tasks/`, общий пакет `common/`, `Main.java` и `run.bat` | `javac` + `java`, без сборщика |

## Как запустить

Лабораторная 1 — достаточно JDK:

```
cd WORK_Java/laboratory_work_1
javac 01_OneContractTwoImplementations.java
java -cp . OneContractTwoImplementations
```

Лабораторная 2 — нужен Maven:

```
cd WORK_Java/laboratory_work_2
mvn test
```

Лабораторная 3 — достаточно JDK 21 (в каждой папке-задании есть `run.bat`):

```
cd WORK_Java/laboratory_work_3
javac -encoding UTF-8 task_01_list_order\*.java
java -Dstdout.encoding=UTF-8 -cp . task_01_list_order.task_01_list_order
```

Лабораторная 4 — достаточно JDK 21 (есть `run.bat` и общий `Main.java`):

```
cd WORK_Java/laboratory_work_4
javac -encoding UTF-8 -d out common\*.java tasks\*.java Main.java
java -Dstdout.encoding=UTF-8 -cp out Main
```

Подробности по каждой лабораторной — в её README.

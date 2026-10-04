# Лабораторная работа 1. Полиморфизм и проектирование

15 заданий, каждое — самостоятельный `.java`-файл с классом и методом `main`.
Комментарии `/** ... */` описывают суть решения над каждым классом и методом.

Сборка всех заданий сразу:

```
javac *.java
```

## Уровень I. Полиморфизм

| Файл | Задание |
|---|---|
| `01_OneContractTwoImplementations.java` | Один контракт, две реализации |
| `02_DynamicDispatchOnPaper.java` | Dynamic dispatch на бумаге |
| `03_PolymorphicArray.java` | Полиморфный массив |

## Уровень II. Абстракция и наследование

| Файл | Задание |
|---|---|
| `04_AbstractBaseClass.java` | Абстрактный базовый класс |
| `05_InterfaceOrAbstractClass.java` | Интерфейс или абстрактный класс |
| `06_OverrideVsOverload.java` | Переопределение без случайной перегрузки |
| `07_CompositionOverInheritance.java` | Композиция вместо наследования |

## Уровень III. Контракты подтипов

| Файл | Задание |
|---|---|
| `08_LspViolation.java` | Нарушение LSP |
| `09_EnrollmentPolicyStrategy.java` | Политика допуска как стратегия |
| `10_DefaultMethodConflict.java` | Конфликт default-методов |

## Уровень IV. Закрытые и открытые иерархии

| Файл | Задание | Требования к JDK |
|---|---|---|
| `11_EqualityInHierarchy.java` | Равенство в иерархии | JDK 8+ |
| `12_SealedHierarchyResult.java` | Sealed-иерархия результата | **JDK 21** |

## Уровень V. Проектирование

| Файл | Задание | Требования к JDK |
|---|---|---|
| `13_RemoveTypeCode.java` | Удаление type code | JDK 8+ |
| `14_InheritanceExplosion.java` | Наследование взрывает комбинации | JDK 8+ |
| `15_CourseHubEngine.java` | Расширяемый движок правил | **JDK 21** |

Задания 12 и 15 используют `sealed`, `record` и pattern matching для `switch`,
поэтому требуют `javac --release 21`. Остальные собираются на JDK 8 и новее.

Текст заданий — в `message.txt`.

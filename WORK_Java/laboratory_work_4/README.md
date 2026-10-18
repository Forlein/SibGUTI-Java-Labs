# Лабораторная работа 4. Контракты, полиморфизм и проектирование в Java

Проект разделён на две части:

- `tasks/` — отдельная папка с 15 чистыми задачами (`Task01OneContractTwoImplementations.java` … `Task15ExtensibleRuleEngine.java`), у каждой задачи собственный метод `main`;
- `common/` — общие доменные классы и интерфейсы вне папки задач (`Identifiable`, `BaseEntity`, `Course`, `Student`, `Notification`, `NotificationSender`, `CourseFormatter`, `CompactCourseFormatter`, `DetailedCourseFormatter`, `ClockProvider`).

Сборка и запуск всех заданий подряд (требуется JDK 21):

```
javac -encoding UTF-8 -d out common\*.java tasks\*.java Main.java
java -Dstdout.encoding=UTF-8 -cp out Main
```

Либо через `run.bat`:

```
.\run.bat
.\run.bat Task01OneContractTwoImplementations
```

Готовый отчёт по работе — `Отчет_ЛР4_Java.docx`.

## Задания

| Файл в `tasks/` | Задание |
|---|---|
| `Task01OneContractTwoImplementations.java` | Один контракт (`CourseFormatter`), две реализации, compile-time и runtime типы |
| `Task02DynamicDispatchOnPaper.java` | Dynamic dispatch на бумаге, overload resolution против override dispatch |
| `Task03PolymorphicArray.java` | Полиморфный массив `CourseFormatter[]` без `if` по типу + 3-я реализация |
| `Task04AbstractBaseClass.java` | Абстрактный базовый класс `AbstractNotificationSender` с `final send` и `abstract doSend` |
| `Task05InterfaceOrAbstractClass.java` | Выбор `interface` или `abstract class` для 5 понятий с мотивировкой и ценой изменения |
| `Task06OverrideWithoutAccidentalOverload.java` | Переопределение с `@Override` против случайной перегрузки |
| `Task07CompositionOverInheritance.java` | Декораторы `Logging`, `Metrics`, `Retry` вокруг `NotificationSender` и два порядка композиции |
| `Task08LspViolation.java` | Устранение нарушения LSP в `ReadOnlyCourseRepository` через разделение интерфейсов |
| `Task09EnrollmentPolicyStrategy.java` | Политика допуска `EnrollmentPolicy` как стратегия, внедряемая в `EnrollmentService` |
| `Task10DefaultMethodConflict.java` | Конфликт `default`-методов двух интерфейсов и явное разрешение через `Interface.super` |
| `Task11EqualityInHierarchy.java` | Нарушение симметричности `Point`/`ColoredPoint`, strict equality, композиция цвета и value class |
| `Task12SealedHierarchyResult.java` | `sealed interface EnrollmentResult`, exhaustive `switch` без `default` и добавление `RequiresApproval` |
| `Task13RemoveTypeCode.java` | Удаление строкового `notificationType` и граница оправданного полиморфизма |
| `Task14InheritanceExplosion.java` | Разделение форматов курса и политик оплаты на независимые компоненты вместо 9 подклассов |
| `Task15ExtensibleRuleEngine.java` | Расширяемый движок правил CourseHub (`allOf`, `anyOf`, `sealed`-вердикты, диаграмма и записка) |

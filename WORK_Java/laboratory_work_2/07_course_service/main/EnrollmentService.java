package ru.sibguti.lab2.domain;

/*
 * КОПИЯ ФАЙЛА. Создан в задании 7, перенесён в задание 7 без изменений.
 * Зачем он нужен в этом задании: таблица решений R1..R5: возвращает решение, а не бросает исключение.
 * Копия нужна для автономности папки задания: правьте её здесь, остальные задания не затронуты.
 * Класс EnrollmentService содержит таблицу решений «зачислить или отказать»
 * в виде обычной цепочки if/else. Это и есть решение из задания 13:
 * четыре условия (статус курса, свободные места, prerequisite, повторная заявка)
 * проверяются строго в одном порядке, и порядок сам является частью контракта,
 * потому что первое сработавшее правило и определяет ответ.
 * Зависимости: его вызывает CourseHubFacade (задание 15), когда применяет
 * бизнес-правила к реальному курсу и реальному студенту, и его напрямую
 * проверяют тесты задания 13, потому что метод чистый и не имеет состояния.
 */
public class EnrollmentService {

    public EnrollmentDecision decide(CourseStatus status, boolean hasFreeSeats,
                                     boolean prerequisiteMet, boolean alreadyApplied) {
        if (status != CourseStatus.OPEN) {
            return EnrollmentDecision.rejected(ErrorCode.COURSE_NOT_OPEN, "R1",
                    "статус курса не OPEN, остальные условия не проверяются");
        }
        if (alreadyApplied) {
            return EnrollmentDecision.rejected(ErrorCode.ALREADY_ENROLLED, "R2",
                    "повторная заявка запрещена");
        }
        if (!hasFreeSeats) {
            return EnrollmentDecision.rejected(ErrorCode.NO_FREE_SEATS, "R3",
                    "свободных мест нет");
        }
        if (!prerequisiteMet) {
            return EnrollmentDecision.rejected(ErrorCode.PREREQUISITE_MISSING, "R4",
                    "не закрыт prerequisite");
        }
        return EnrollmentDecision.accepted("R5", "все условия выполнены");
    }
}

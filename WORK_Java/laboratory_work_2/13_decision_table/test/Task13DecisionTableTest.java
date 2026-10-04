package ru.sibguti.lab2.task13;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sibguti.lab2.domain.CourseStatus;
import ru.sibguti.lab2.domain.EnrollmentDecision;
import ru.sibguti.lab2.domain.EnrollmentService;
import ru.sibguti.lab2.domain.ErrorCode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Задание 13. Таблица решений и тесты (часть 2).
 * Класс Task13DecisionTableTest содержит шесть тестов - по одному на каждую
 * из шести существенных строк, которые различаются разными правилами.
 * Седьмая существенная строка (закрытый набор) проверяется параметризованным
 * тестом внизу: для неё нет отдельного правила, отдельный тест не нужен.
 * Остальные 25 комбинаций удаляются как дублирующие: они дают тот же код отказа
 * по тому же правилу и не увеличивают покрытие.
 * Последний параметризованный тест не дублирует правила, а сверяет таблицу
 * с реализацией целиком: это защита от того, что таблица и код разойдутся.
 * Зависимости: использует EnrollmentService (реализацию правил), CourseStatus,
 * EnrollmentDecision, ErrorCode и саму таблицу Task13DecisionTable.
 */
class Task13DecisionTableTest {

    private final EnrollmentService service = new EnrollmentService();

    /*
     * Тест строки 1 таблицы: курс ещё не открыт, но места есть, prerequisite
     * закрыт и заявки не было. Отказ должен быть из-за статуса, остальные
     * условия не проверяются вовсе.
     */
    @Test
    @DisplayName("R1: неоткрытый курс отклоняется даже при свободных местах")
    void newCourseIsRejectedEvenWithFreeSeats() {
        EnrollmentDecision decision = service.decide(CourseStatus.NEW, true, true, false);

        assertAll(
                () -> assertFalse(decision.isAccepted(), "зачисление в неоткрытый курс невозможно"),
                () -> assertEquals(ErrorCode.COURSE_NOT_OPEN, decision.getError(), "код отказа"),
                () -> assertEquals("R1", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Тест строки 2 таблицы: курс отменён, мест нет, prerequisite не закрыт
     * и заявка уже была. Отказ всё равно из-за статуса - это доказывает,
     * что правило R1 не зависит от остальных условий.
     */
    @Test
    @DisplayName("R1: отменённый курс отклоняется даже при повторной заявке")
    void cancelledCourseIsRejectedEvenWithRepeatedApplication() {
        EnrollmentDecision decision = service.decide(CourseStatus.CANCELLED, false, false, true);

        assertAll(
                () -> assertFalse(decision.isAccepted(), "зачисление в отменённый курс невозможно"),
                () -> assertEquals(ErrorCode.COURSE_NOT_OPEN, decision.getError(), "код отказа"),
                () -> assertEquals("R1", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Тест строки 3 таблицы: курс открыт, места есть, prerequisite закрыт,
     * но студент уже подавал заявку. Места и prerequisite здесь в идеале,
     * поэтому отказ целиком из-за повторной заявки.
     */
    @Test
    @DisplayName("R2: повторная заявка отклоняется при идеальных остальных условиях")
    void repeatedApplicationIsRejectedOnPerfectCourse() {
        EnrollmentDecision decision = service.decide(CourseStatus.OPEN, true, true, true);

        assertAll(
                () -> assertFalse(decision.isAccepted(), "повторная заявка не зачисляется"),
                () -> assertEquals(ErrorCode.ALREADY_ENROLLED, decision.getError(), "код отказа"),
                () -> assertEquals("R2", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Тест строки 4 таблицы: курс открыт, prerequisite закрыт, заявки не было,
     * но мест нет. Отказ из-за мест показывает, что правило R3 не зависит
     * от остальных условий.
     */
    @Test
    @DisplayName("R3: отсутствие мест отклоняет заявку")
    void courseWithoutFreeSeatsIsRejected() {
        EnrollmentDecision decision = service.decide(CourseStatus.OPEN, false, true, false);

        assertAll(
                () -> assertFalse(decision.isAccepted(), "без мест заявка не принимается"),
                () -> assertEquals(ErrorCode.NO_FREE_SEATS, decision.getError(), "код отказа"),
                () -> assertEquals("R3", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Тест строки 5 таблицы: курс открыт, места есть, заявки не было,
     * но prerequisite не закрыт. Это единственная комбинация правила R4.
     */
    @Test
    @DisplayName("R4: незакрытый prerequisite отклоняет заявку")
    void missingPrerequisiteIsRejected() {
        EnrollmentDecision decision = service.decide(CourseStatus.OPEN, true, false, false);

        assertAll(
                () -> assertFalse(decision.isAccepted(), "без prerequisite заявка не принимается"),
                () -> assertEquals(ErrorCode.PREREQUISITE_MISSING, decision.getError(), "код отказа"),
                () -> assertEquals("R4", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Тест строки 6 таблицы: все условия выполнены, зачисление разрешено.
     * Это единственная комбинация правила R5.
     */
    @Test
    @DisplayName("R5: выполнены все условия, зачисление разрешено")
    void allConditionsMetMeansAccepted() {
        EnrollmentDecision decision = service.decide(CourseStatus.OPEN, true, true, false);

        assertAll(
                () -> assertTrue(decision.isAccepted(), "при выполненных условиях зачисление разрешено"),
                () -> assertEquals("R5", decision.getRule(), "сработавшее правило")
        );
    }

    /*
     * Параметризованный тест сверяет таблицу с реализацией на всех 32 строках.
     * Он не добавляет покрытия правил (они уже проверены шестью тестами выше),
     * но защищает от расхождения таблицы и кода: если правило поменяют,
     * тест укажет точную строку, где ожидание разошлось с поведением.
     * Данные берутся из метода allRows по аннотации @MethodSource.
     */
    @ParameterizedTest(name = "{0}: статус {1}, места {2}, prerequisite {3}, заявка {4}")
    @MethodSource("allRows")
    @DisplayName("реализация совпадает с таблицей решений на всех комбинациях")
    void implementationMatchesTable(String number, CourseStatus status, boolean freeSeats,
                                    boolean prerequisiteMet, boolean alreadyApplied, boolean accepted,
                                    ErrorCode code, String rule) {
        EnrollmentDecision decision = service.decide(status, freeSeats, prerequisiteMet, alreadyApplied);

        assertAll(
                () -> assertEquals(accepted, decision.isAccepted(),
                        "строка " + number + " таблицы: ожидалось решение " + accepted),
                () -> assertEquals(code, decision.getError(),
                        "строка " + number + " таблицы: ожидался код отказа " + code),
                () -> assertEquals(rule, decision.getRule(),
                        "строка " + number + " таблицы: ожидалось правило " + rule)
        );
    }

    /*
     * Метод allRows разворачивает таблицу Task13DecisionTable в набор аргументов
     * для параметризованного теста. Его вызывает аннотация @MethodSource.
     * Значения берутся из таблицы, поэтому новые правила попадут в тест сами.
     */
    static List<org.junit.jupiter.params.provider.Arguments> allRows() {
        Task13DecisionTable.Row[] rows = Task13DecisionTable.buildAllRows();
        List<org.junit.jupiter.params.provider.Arguments> cases =
                new ArrayList<org.junit.jupiter.params.provider.Arguments>();
        for (int i = 0; i < rows.length; i++) {
            Task13DecisionTable.Row row = rows[i];
            cases.add(org.junit.jupiter.params.provider.Arguments.of(
                    String.valueOf(i + 1),
                    row.getStatus(),
                    row.hasFreeSeats(),
                    row.isPrerequisiteMet(),
                    row.isAlreadyApplied(),
                    row.isAccepted(),
                    row.getExpectedCode(),
                    row.getRule()));
        }
        return cases;
    }
}

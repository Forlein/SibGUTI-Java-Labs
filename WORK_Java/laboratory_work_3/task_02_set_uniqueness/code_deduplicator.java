package task_02_set_uniqueness;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class code_deduplicator {

    public static Set<String> deduplicate(List<String> course_code_list) {
        /* Убирает дубликаты кодов курса через LinkedHashSet, сохраняя первый порядок появления. */
        return new LinkedHashSet<>(course_code_list);
    }

    public static List<String> find_skipped_duplicates(List<String> course_code_list) {
        /* Возвращает коды, которые были пропущены как повторы; add возвращает false при дубликате. */
        Set<String> unique_codes = new LinkedHashSet<>();
        List<String> skipped_duplicates = new ArrayList<>();
        for (String course_code : course_code_list) {
            if (!unique_codes.add(course_code)) {
                skipped_duplicates.add(course_code);
            }
        }
        return skipped_duplicates;
    }
}

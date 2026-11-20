package org.twins.core.unit.dto.twin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.twins.core.dto.rest.twin.TwinGroupLevelDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchRqDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bean-validation contract of the grouped search request: levels count (1..3) and non-null elements of
 * levels/memberSorts are rejected at the DTO boundary (400 VALIDATION_DTO_FAILED), so the service trusts
 * the converted lists.
 */
class TwinGroupedSearchRqDTOv1ValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestPasses() {
        assertTrue(violations(request(List.of(level(), level()), null)).isEmpty());
    }

    @Test
    void moreThanThreeLevelsIsRejected() {
        assertFalse(violations(request(List.of(level(), level(), level(), level()), null)).isEmpty());
    }

    @Test
    void emptyLevelsIsRejected() {
        assertFalse(violations(request(List.of(), null)).isEmpty());
    }

    @Test
    void nullLevelElementIsRejected() {
        List<TwinGroupLevelDTOv1> levels = new ArrayList<>();
        levels.add(level());
        levels.add(null);
        assertFalse(violations(request(levels, null)).isEmpty());
    }

    @Test
    void moreThanTwoMemberSortsIsRejected() {
        assertFalse(violations(request(List.of(level()), List.of(sort(), sort(), sort()))).isEmpty());
    }

    @Test
    void nullMemberSortElementIsRejected() {
        List<TwinSortDTOv1> sorts = new ArrayList<>();
        sorts.add(null);
        assertFalse(violations(request(List.of(level()), sorts)).isEmpty());
    }

    private Set<ConstraintViolation<TwinGroupedSearchRqDTOv1>> violations(TwinGroupedSearchRqDTOv1 request) {
        return validator.validate(request);
    }

    private static TwinGroupedSearchRqDTOv1 request(List<TwinGroupLevelDTOv1> levels, List<TwinSortDTOv1> memberSorts) {
        return new TwinGroupedSearchRqDTOv1()
                .setSearch(new TwinSearchDTOv1())
                .setLevels(levels)
                .setMemberSorts(memberSorts);
    }

    private static TwinGroupLevelDTOv1 level() {
        return new TwinGroupLevelDTOv1().setFieldId(UUID.randomUUID());
    }

    private static TwinSortDTOv1 sort() {
        return new TwinSortDTOv1().setTwinClassFieldId(UUID.randomUUID());
    }
}

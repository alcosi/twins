package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.exception.ErrorCodeTwins;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TwinFinderByFieldDateCurrentDateTest {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 26);

    @Test
    void startOfDayIsMidnight() throws ServiceException {
        assertEquals(LocalDateTime.of(2026, 11, 26, 0, 0),
                TwinFinderByFieldDateCurrentDate.resolveBound(DAY, TwinFinderByFieldDateCurrentDate.BOUNDARY_START_OF_DAY));
    }

    @Test
    void endOfDayIsMaxTime() throws ServiceException {
        assertEquals(LocalDateTime.of(2026, 11, 26, 23, 59, 59, 999_999_999),
                TwinFinderByFieldDateCurrentDate.resolveBound(DAY, TwinFinderByFieldDateCurrentDate.BOUNDARY_END_OF_DAY));
    }

    @Test
    void nowIsCloseToCurrentMoment() throws ServiceException {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime bound = TwinFinderByFieldDateCurrentDate.resolveBound(DAY, TwinFinderByFieldDateCurrentDate.BOUNDARY_NOW);
        LocalDateTime after = LocalDateTime.now();
        assertEquals(DAY.atStartOfDay(), bound.toLocalDate().atStartOfDay());
        org.junit.jupiter.api.Assertions.assertTrue(!bound.isBefore(before) && !bound.isAfter(after));
    }

    @Test
    void unknownBoundaryFails() {
        ServiceException se = assertThrows(ServiceException.class,
                () -> TwinFinderByFieldDateCurrentDate.resolveBound(DAY, "YESTERDAY"));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), se.getErrorCode());
    }
}

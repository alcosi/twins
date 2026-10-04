package org.twins.core.unit.search;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.domain.search.ConcatenatedPageWindow;
import org.twins.core.domain.search.ConcatenatedPageWindow.Slice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcatenatedPageWindowTest {

    @Test
    void pageInsideFirstSegment() throws ServiceException {
        assertEquals(List.of(new Slice(0, 0, 50)), ConcatenatedPageWindow.slices(new long[]{120, 400}, 0, 50));
        assertEquals(List.of(new Slice(0, 50, 50)), ConcatenatedPageWindow.slices(new long[]{120, 400}, 50, 50));
    }

    @Test
    void pageStraddlesBoundary() throws ServiceException {
        assertEquals(
                List.of(new Slice(0, 100, 20), new Slice(1, 0, 30)),
                ConcatenatedPageWindow.slices(new long[]{120, 400}, 100, 50));
    }

    @Test
    void pagePastFirstSegmentShiftsOffset() throws ServiceException {
        assertEquals(List.of(new Slice(1, 30, 50)), ConcatenatedPageWindow.slices(new long[]{120, 400}, 150, 50));
    }

    @Test
    void firstPageTakesRemainderFromSecondSegment() throws ServiceException {
        assertEquals(
                List.of(new Slice(0, 0, 20), new Slice(1, 0, 10)),
                ConcatenatedPageWindow.slices(new long[]{20, 100}, 0, 30));
        assertEquals(List.of(new Slice(1, 10, 30)), ConcatenatedPageWindow.slices(new long[]{20, 100}, 30, 30));
    }

    @Test
    void secondPageSkipsShortFirstSegment() throws ServiceException {
        assertEquals(520, ConcatenatedPageWindow.total(new long[]{120, 400}));
        assertEquals(20, ConcatenatedPageWindow.total(new long[]{4, 16}));
        assertEquals(List.of(new Slice(1, 6, 10)), ConcatenatedPageWindow.slices(new long[]{4, 16}, 10, 10));
    }

    @Test
    void threeSegments() throws ServiceException {
        assertEquals(
                List.of(new Slice(1, 5, 5), new Slice(2, 0, 5)),
                ConcatenatedPageWindow.slices(new long[]{10, 10, 10}, 15, 10));
        assertEquals(List.of(new Slice(1, 0, 10)), ConcatenatedPageWindow.slices(new long[]{10, 10, 10}, 10, 10));
    }

    @Test
    void emptySegmentIsSkippedAndPagePastEndIsEmpty() throws ServiceException {
        assertEquals(List.of(new Slice(1, 0, 5)), ConcatenatedPageWindow.slices(new long[]{0, 8}, 0, 5));
        assertEquals(List.of(), ConcatenatedPageWindow.slices(new long[]{5, 5}, 20, 10));
        assertEquals(List.of(new Slice(0, 0, 5)), ConcatenatedPageWindow.slices(new long[]{5}, 0, 10));
    }
}

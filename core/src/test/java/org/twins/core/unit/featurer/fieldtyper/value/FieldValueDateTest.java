package org.twins.core.unit.featurer.fieldtyper.value;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueDate extends FieldValueStated. date+pattern are kept private;
 * setDate transitions state to PRESENT or CLEARED based on null/empty input.
 * getDateStr formats via the constructor pattern.
 */
class FieldValueDateTest extends BaseUnitTest {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class SetDate {

        @Test
        void setDate_string_transitionsToPresent() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);

            var returned = value.setDate("2026-06-17 10:15:30");

            assertSame(value, returned);
            assertNotNull(value.getDate());
            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setDate_emptyString_clears() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            value.setDate("");

            assertNull(value.getDate());
            assertTrue(value.isCleared());
        }

        @Test
        void setDate_nullString_clears() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);

            value.setDate((String) null);

            assertNull(value.getDate());
            assertTrue(value.isCleared());
        }

        @Test
        void setDate_localDateTime_present() {
            var value = new FieldValueDate(field, PATTERN);
            var now = LocalDateTime.of(2026, 6, 17, 10, 15, 30);

            var returned = value.setDate(now);

            assertSame(value, returned);
            assertEquals(now, value.getDate());
            assertFalse(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void setDate_nullLocalDateTime_clears() {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate(LocalDateTime.now());

            value.setDate((LocalDateTime) null);

            assertNull(value.getDate());
            assertTrue(value.isCleared());
        }
    }

    @Nested
    class GetDateStr {

        @Test
        void getDateStr_whenSet_returnsFormattedValue() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            assertEquals("2026-06-17 10:15:30", value.getDateStr());
        }

        @Test
        void getDateStr_whenNull_returnsNull() {
            var value = new FieldValueDate(field, PATTERN);

            assertNull(value.getDateStr());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingFormattedString_returnsTrue() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            assertTrue(value.hasValue("2026-06-17 10:15:30"));
        }

        @Test
        void hasValue_mismatchingString_returnsFalse() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            assertFalse(value.hasValue("1999-01-01 00:00:00"));
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void clear_wipesDate() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            value.clear();

            assertNull(value.getDate());
            assertTrue(value.isCleared());
        }

        @Test
        void undefine_wipesDate() throws ServiceException {
            var value = new FieldValueDate(field, PATTERN);
            value.setDate("2026-06-17 10:15:30");

            value.undefine();

            assertNull(value.getDate());
            assertTrue(value.isUndefined());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_copiesDateAndPattern() throws ServiceException {
            var src = new FieldValueDate(field, PATTERN);
            src.setDate("2026-06-17 10:15:30");
            // destination built with a different pattern; copy must override it
            var dst = new FieldValueDate(field, "dd-MM-yyyy");

            src.copyValueTo(dst);

            assertEquals(src.getDate(), dst.getDate());
            assertEquals("2026-06-17 10:15:30", dst.getDateStr());
        }
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsDateWithNullPattern() {
            var src = new FieldValueDate(field, PATTERN);

            var created = src.newInstance(field);

            assertInstanceOf(FieldValueDate.class, created);
            assertNotSame(src, created);
        }
    }
}

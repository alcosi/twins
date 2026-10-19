package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueAttachment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FieldValueAttachment extends FieldValueStated and carries a (name, base64Content)
 * pair. setX transitions to PRESENT, onClear/onUndefine wipe both fields.
 * hasValue compares against base64Content only (intentional contract — content is
 * the meaningful payload, the name is just metadata).
 */
class FieldValueAttachmentTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    @Nested
    class Setters {

        @Test
        void setName_isChainedAndStored() {
            var value = new FieldValueAttachment(field);

            var returned = value.setName("logo.png");

            assertSame(value, returned);
            assertEquals("logo.png", value.getName());
        }

        @Test
        void setBase64Content_isChainedAndStored() {
            var value = new FieldValueAttachment(field);

            var returned = value.setBase64Content("QUJD");

            assertSame(value, returned);
            assertEquals("QUJD", value.getBase64Content());
        }

        @Test
        void setters_doNotTouchUndefinedState() {
            // FieldValueStated state is UNDEFINED until an explicit state transition
            // (clear/undefine) or until updateMutableValueState fires. Setters on
            // FieldValueAttachment are plain field assignments, not state transitions.
            var value = new FieldValueAttachment(field);
            value.setName("a");
            value.setBase64Content("b");

            // state stays UNDEFINED — there is no PRESENT transition defined for setters
            assertTrue(value.isUndefined());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingBase64_returnsTrue() {
            var value = new FieldValueAttachment(field);
            value.setBase64Content("QUJD");

            assertTrue(value.hasValue("QUJD"));
        }

        @Test
        void hasValue_mismatchingBase64_returnsFalse() {
            var value = new FieldValueAttachment(field);
            value.setBase64Content("QUJD");

            assertFalse(value.hasValue("WA=="));
        }

        @Test
        void hasValue_whenContentNull_returnsFalseForNonNullArg() {
            var value = new FieldValueAttachment(field);

            assertFalse(value.hasValue("QUJD"));
        }

        @Test
        void hasValue_whenContentNullAndArgNull_returnsTrue() {
            // Objects.equals(null, null) == true — pinned as intended null-safe contract.
            var value = new FieldValueAttachment(field);

            assertTrue(value.hasValue(null));
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void clear_wipesNameAndContentAndMarksCleared() {
            var value = new FieldValueAttachment(field);
            value.setName("a");
            value.setBase64Content("b");

            value.clear();

            assertNull(value.getName());
            assertNull(value.getBase64Content());
            assertTrue(value.isCleared());
            assertFalse(value.isUndefined());
        }

        @Test
        void undefine_wipesNameAndContentAndMarksUndefined() {
            var value = new FieldValueAttachment(field);
            value.setName("a");
            value.setBase64Content("b");

            value.undefine();

            assertNull(value.getName());
            assertNull(value.getBase64Content());
            assertTrue(value.isUndefined());
            assertFalse(value.isCleared());
        }

        @Test
        void newInstance_isFreshUndefined() {
            var src = new FieldValueAttachment(field);
            src.setName("a");

            var created = src.newInstance(field);

            assertInstanceOf(FieldValueAttachment.class, created);
            assertNotSame(src, created);
            assertNull(((FieldValueAttachment) created).getName());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_copiesBothFields() {
            var src = new FieldValueAttachment(field);
            src.setName("logo.png");
            src.setBase64Content("QUJD");
            var dst = new FieldValueAttachment(field);

            src.copyValueTo(dst);

            assertEquals("logo.png", dst.getName());
            assertEquals("QUJD", dst.getBase64Content());
        }

        @Test
        void copyValueTo_overwritesExistingDestinationFields() {
            var src = new FieldValueAttachment(field);
            src.setName("a");
            src.setBase64Content("b");
            var dst = new FieldValueAttachment(field);
            dst.setName("old");
            dst.setBase64Content("oldB64");

            src.copyValueTo(dst);

            assertEquals("a", dst.getName());
            assertEquals("b", dst.getBase64Content());
        }
    }
}

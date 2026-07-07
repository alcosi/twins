package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueLinkTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinLinkEntity link(UUID dstTwinId) {
        return new TwinLinkEntity().setDstTwinId(dstTwinId);
    }

    @Nested
    class StateByCollection {

        @Test
        void newInstance_collectionNull_isUndefined() {
            var value = new FieldValueLink(field);

            assertTrue(value.isUndefined());
            assertTrue(value.isEmpty());
            assertEquals(0, value.size());
        }

        @Test
        void addFirstItem_marksDefined() {
            var value = new FieldValueLink(field);

            value.add(link(UUID.randomUUID()));

            assertFalse(value.isUndefined());
            assertEquals(1, value.size());
        }

        @Test
        void clear_isClearedWithEmptyNonNullCollection() {
            var value = new FieldValueLink(field);
            value.add(link(UUID.randomUUID()));

            value.clear();

            assertTrue(value.isCleared());
            assertFalse(value.isUndefined());
            assertEquals(0, value.size());
        }

        @Test
        void undefine_setsCollectionNull() {
            var value = new FieldValueLink(field);
            value.add(link(UUID.randomUUID()));

            value.undefine();

            assertTrue(value.isUndefined());
            assertNull(value.getItems());
        }
    }

    @Nested
    class AddAndSetItems {

        @Test
        void add_nullItem_isNoOp() {
            var value = new FieldValueLink(field);

            value.add(null);

            assertEquals(0, value.size());
            assertTrue(value.isUndefined());
        }

        @Test
        void add_nullifyMarkerDstId_clearsField() {
            var value = new FieldValueLink(field);
            value.add(link(UUID.randomUUID()));
            var nullify = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

            value.add(link(nullify));

            assertTrue(value.isCleared());
        }

        @Test
        void setItems_nonEmpty_replacesContents() {
            var value = new FieldValueLink(field);
            value.add(link(UUID.randomUUID()));
            var b = link(UUID.randomUUID());

            value.setItems(List.of(b));

            assertEquals(1, value.size());
            assertEquals(b.getDstTwinId(), value.getItems().get(0).getDstTwinId());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchesDstTwinId_returnsTrue() {
            var dstId = UUID.randomUUID();
            var value = new FieldValueLink(field);
            value.add(link(dstId));

            assertTrue(value.hasValue(dstId.toString()));
        }

        @Test
        void hasValue_emptyCollection_returnsFalse() {
            var value = new FieldValueLink(field);

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }
    }

    @Nested
    class ForwardLinkFlag {

        @Test
        void forwardLink_defaultsFalse_andIsSettable() {
            var value = new FieldValueLink(field);

            assertFalse(value.isForwardLink());

            value.setForwardLink(true);

            assertTrue(value.isForwardLink());
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_clonesEachLinkAndPreservesForwardFlag() {
            var src = new FieldValueLink(field);
            var dstId = UUID.randomUUID();
            src.add(link(dstId));
            src.setForwardLink(true);
            var dst = new FieldValueLink(field);

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
            // each link is cloned into a distinct entity
            assertNotSame(src.getItems().get(0), dst.getItems().get(0));
            assertEquals(dstId, dst.getItems().get(0).getDstTwinId());
            assertTrue(dst.isForwardLink());
        }

        /**
         * BUG: FieldValueLink.copyValueTo iterates the raw {@code collection} field without a
         * null guard, while its parent FieldValueCollection.copyValueTo explicitly handles
         * collection == null. Copying an undefined (never-populated) link field therefore
         * throws NPE instead of producing an undefined/cleared destination.
         * Intended behavior: mirror the parent — undefined source yields undefined destination.
         * Awaiting user decision: fix the prod code (add null guard) vs. encode the NPE.
         */
        @Test
        void copyValueTo_fromUndefinedSource_producesUndefinedDestination() {
            var src = new FieldValueLink(field); // never populated -> collection == null
            var dst = new FieldValueLink(field);

            src.copyValueTo(dst);

            assertTrue(dst.isUndefined());
            assertEquals(0, dst.size());
        }

        @Test
        void copyValueTo_fromClearedSource_producesEmptyDestination() {
            var src = new FieldValueLink(field);
            src.clear(); // empty non-null collection
            var dst = new FieldValueLink(field);

            src.copyValueTo(dst);

            assertEquals(0, dst.size());
        }
    }

    @Nested
    class Clone {

        @Test
        @Disabled("bug #1: base FieldValue classes lack @EqualsAndHashCode → equals is identity, so clone never equals original. Re-enable once equals/hashCode is fixed (see FieldValue TODO).")
        void clone_producesEqualIndependentCopy() {
            var original = new FieldValueLink(field);
            var dstId = UUID.randomUUID();
            original.add(link(dstId));

            var clone = original.clone();

            assertNotSame(original, clone);
            assertEquals(original, clone);
            assertEquals(1, ((FieldValueLink) clone).size());
            assertEquals(dstId, ((FieldValueLink) clone).getItems().get(0).getDstTwinId());
        }
    }
}

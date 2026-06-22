package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueAliases;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FieldValueAliases is a FieldValueCollectionImmutable<TwinAliasEntity>.
 * Nullify-marker handling, state transitions, getItems immutability and
 * copyValueTo are all inherited — these tests pin the alias-specific contract:
 * the id function is TwinAliasEntity::getId, and newInstance yields an Aliases.
 */
class FieldValueAliasesTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private TwinAliasEntity alias(UUID id) {
        var a = mock(TwinAliasEntity.class);
        when(a.getId()).thenReturn(id);
        return a;
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_producesAliasesWithNewField() {
            var src = new FieldValueAliases(field);
            var newField = new TwinClassFieldEntity();

            var created = src.newInstance(newField);

            assertInstanceOf(FieldValueAliases.class, created);
            assertSame(newField, created.getTwinClassField());
        }
    }

    @Nested
    class AddAndState {

        @Test
        void add_singleItem_transitionsToPresent() {
            var value = new FieldValueAliases(field);

            var returned = value.add(alias(UUID.randomUUID()));

            assertSame(value, returned);
            assertEquals(1, value.size());
            assertTrue(value.isDefined());
            assertFalse(value.isCleared());
            assertFalse(value.isUndefined());
        }

        @Test
        void add_nullItem_isNoOp() {
            var value = new FieldValueAliases(field);

            value.add(null);

            assertEquals(0, value.size());
            assertTrue(value.isUndefined());
        }

        @Test
        void add_nullifyMarker_clearsAndDropsCollection() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            value.add(alias(FieldValueAliasesTest.nullifyMarker()));

            assertTrue(value.isCleared());
            assertEquals(0, value.size());
        }
    }

    @Nested
    class SetItems {

        @Test
        void setItems_nonEmpty_transitionsToPresentAndCopies() {
            var value = new FieldValueAliases(field);
            var first = alias(UUID.randomUUID());

            value.setItems(List.of(first));
            var originalCollection = value.getItems();

            // mutating the source must not change the stored collection
            value.setItems(List.of(first, alias(UUID.randomUUID())));

            assertEquals(2, value.size());
            assertNotSame(originalCollection, value.getItems());
        }

        @Test
        void setItems_emptyCollection_clears() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
        }

        @Test
        void setItems_nullifyMarker_clears() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            value.setItems(List.of(alias(nullifyMarker())));

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingIdString_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueAliases(field);
            value.add(alias(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            assertFalse(value.hasValue("not-a-uuid"));
        }

        @Test
        void hasValue_whenUndefined_returnsFalse() {
            var value = new FieldValueAliases(field);

            assertFalse(value.hasValue(UUID.randomUUID().toString()));
        }
    }

    @Nested
    class GetItems {

        @Test
        void getItems_whenEmpty_returnsImmutableEmptyList() {
            var value = new FieldValueAliases(field);

            var items = value.getItems();

            assertNotNull(items);
            assertTrue(items.isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> items.add(alias(UUID.randomUUID())));
        }

        @Test
        void getItems_returnsUnmodifiableView() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            var items = value.getItems();

            assertThrows(UnsupportedOperationException.class, () -> items.add(alias(UUID.randomUUID())));
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_overwritesDestinationCollection() {
            var src = new FieldValueAliases(field);
            src.add(alias(UUID.randomUUID()));
            src.add(alias(UUID.randomUUID()));
            var dst = new FieldValueAliases(field);
            dst.add(alias(UUID.randomUUID()));

            src.copyValueTo(dst);

            assertEquals(src.size(), dst.size());
        }

        @Test
        void copyValueTo_emptyDestination_copiesAll() {
            var src = new FieldValueAliases(field);
            src.add(alias(UUID.randomUUID()));
            var dst = new FieldValueAliases(field);

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
        }
    }

    @Nested
    class ClearAndUndefine {

        @Test
        void undefine_dropsCollection() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            var returned = value.undefine();

            assertSame(value, returned);
            assertTrue(value.isUndefined());
            assertEquals(0, value.size());
        }

        @Test
        void clear_setsClearedState() {
            var value = new FieldValueAliases(field);
            value.add(alias(UUID.randomUUID()));

            value.clear();

            assertTrue(value.isCleared());
        }
    }

    private static UUID nullifyMarker() {
        // NULLIFY_MARKER value from org.cambium.common.util.UuidUtils
        return UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    }
}

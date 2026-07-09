package org.twins.core.unit.featurer.fieldtyper.value;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * FieldValueUser is a FieldValueCollectionImmutable<UserEntity>.
 * id function is UserEntity::getId; everything else is inherited.
 */
class FieldValueUserTest extends BaseUnitTest {

    private TwinClassFieldEntity field;

    @BeforeEach
    void setUp() {
        field = new TwinClassFieldEntity();
    }

    private UserEntity user(UUID id) {
        var u = mock(UserEntity.class);
        when(u.getId()).thenReturn(id);
        return u;
    }

    @Nested
    class NewInstance {

        @Test
        void newInstance_yieldsUserWithNewField() {
            var src = new FieldValueUser(field);
            var newField = new TwinClassFieldEntity();

            var created = src.newInstance(newField);

            assertInstanceOf(FieldValueUser.class, created);
            assertSame(newField, created.getTwinClassField());
        }
    }

    @Nested
    class AddAndSetItems {

        @Test
        void add_singleUser_transitionsToPresent() {
            var value = new FieldValueUser(field);

            value.add(user(UUID.randomUUID()));

            assertEquals(1, value.size());
            assertFalse(value.isUndefined());
        }

        @Test
        void add_nullUser_isNoOp() {
            var value = new FieldValueUser(field);

            value.add(null);

            assertEquals(0, value.size());
            assertTrue(value.isUndefined());
        }

        @Test
        void add_multipleUsers_appendsAll() {
            var value = new FieldValueUser(field);

            value.add(user(UUID.randomUUID()));
            value.add(user(UUID.randomUUID()));
            value.add(user(UUID.randomUUID()));

            assertEquals(3, value.size());
        }

        @Test
        void setItems_nonEmpty_transitionsToPresent() {
            var value = new FieldValueUser(field);

            value.setItems(List.of(user(UUID.randomUUID())));

            assertEquals(1, value.size());
            assertFalse(value.isUndefined());
        }

        @Test
        void setItems_empty_clears() {
            var value = new FieldValueUser(field);
            value.add(user(UUID.randomUUID()));

            value.setItems(List.of());

            assertTrue(value.isCleared());
        }
    }

    @Nested
    class HasValue {

        @Test
        void hasValue_matchingUserId_returnsTrue() {
            var id = UUID.randomUUID();
            var value = new FieldValueUser(field);
            value.add(user(id));

            assertTrue(value.hasValue(id.toString()));
        }

        @Test
        void hasValue_nonUuidString_returnsFalse() {
            var value = new FieldValueUser(field);
            value.add(user(UUID.randomUUID()));

            assertFalse(value.hasValue("nope"));
        }
    }

    @Nested
    class CopyValueTo {

        @Test
        void copyValueTo_overwritesDestination() {
            var src = new FieldValueUser(field);
            src.add(user(UUID.randomUUID()));
            src.add(user(UUID.randomUUID()));
            var dst = new FieldValueUser(field);
            dst.add(user(UUID.randomUUID()));

            src.copyValueTo(dst);

            assertEquals(2, dst.size());
        }

        @Test
        void copyValueTo_emptyDestination_copiesAll() {
            var src = new FieldValueUser(field);
            src.add(user(UUID.randomUUID()));
            var dst = new FieldValueUser(field);

            src.copyValueTo(dst);

            assertEquals(1, dst.size());
        }
    }

    @Nested
    class StateLifecycle {

        @Test
        void clear_setsClearedState() {
            var value = new FieldValueUser(field);
            value.add(user(UUID.randomUUID()));

            value.clear();

            assertTrue(value.isCleared());
        }

        @Test
        void undefine_setsUndefinedState() {
            var value = new FieldValueUser(field);
            value.add(user(UUID.randomUUID()));

            value.undefine();

            assertTrue(value.isUndefined());
        }
    }
}

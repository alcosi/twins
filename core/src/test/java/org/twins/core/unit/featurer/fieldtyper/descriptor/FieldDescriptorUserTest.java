package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorUserTest extends BaseUnitTest {

    private FieldDescriptorUser descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorUser();
    }

    private UserEntity user(UUID id) {
        var u = new UserEntity();
        u.setId(id);
        return u;
    }

    @Nested
    class Defaults {

        @Test
        void validUsers_initializedToEmptyList_notNull() {
            assertNotNull(descriptor.validUsers());
            assertTrue(descriptor.validUsers().isEmpty());
        }

        @Test
        void multiple_defaultsToFalse() {
            assertFalse(descriptor.multiple());
        }
    }

    @Nested
    class Add {

        @Test
        void add_appendsUserAndReturnsSelf() {
            var u = user(UUID.randomUUID());
            var returned = descriptor.add(u);

            assertSame(descriptor, returned, "add must be chainable");
            assertEquals(1, descriptor.validUsers().size());
            assertSame(u, descriptor.validUsers().get(0));
        }

        @Test
        void add_severalUsers_preservesInsertionOrder() {
            var first = user(UUID.randomUUID());
            var second = user(UUID.randomUUID());

            descriptor.add(first).add(second);

            assertEquals(2, descriptor.validUsers().size());
            assertSame(first, descriptor.validUsers().get(0));
            assertSame(second, descriptor.validUsers().get(1));
        }
    }
}

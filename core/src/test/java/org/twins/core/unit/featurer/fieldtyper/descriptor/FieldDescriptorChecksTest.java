package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorChecks;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorChecksTest extends BaseUnitTest {

    private FieldDescriptorChecks descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorChecks();
    }

    private DataListOptionEntity option(UUID id) {
        var opt = new DataListOptionEntity();
        opt.setId(id);
        return opt;
    }

    @Nested
    class Defaults {

        @Test
        void options_initializedToEmptyList_notNull() {
            // Unlike FieldDescriptorList (null = long-list flag), Checks inits to an empty mutable list
            assertNotNull(descriptor.options());
            assertTrue(descriptor.options().isEmpty());
        }
    }

    @Nested
    class Add {

        @Test
        void add_appendsOptionAndReturnsSelf() {
            var opt = option(UUID.randomUUID());
            var returned = descriptor.add(opt);

            assertSame(descriptor, returned, "add must be chainable");
            assertEquals(1, descriptor.options().size());
            assertSame(opt, descriptor.options().get(0));
        }

        @Test
        void add_multipleOptions_preservesInsertionOrder() {
            var first = option(UUID.randomUUID());
            var second = option(UUID.randomUUID());

            descriptor.add(first).add(second);

            assertEquals(2, descriptor.options().size());
            assertSame(first, descriptor.options().get(0));
            assertSame(second, descriptor.options().get(1));
        }
    }

    @Nested
    class Equality {

        @Test
        void equals_sameInlineAndOptions_areEqual() {
            var id = UUID.randomUUID();

            var a = new FieldDescriptorChecks();
            a.inline(true);
            a.add(option(id));

            var b = new FieldDescriptorChecks();
            b.inline(true);
            b.add(option(id));

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        void equals_differentInline_areNotEqual() {
            var id = UUID.randomUUID();

            var a = new FieldDescriptorChecks();
            a.inline(true);
            a.add(option(id));

            var b = new FieldDescriptorChecks();
            b.inline(false);
            b.add(option(id));

            assertNotEquals(a, b);
        }
    }
}

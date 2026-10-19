package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorListTest extends BaseUnitTest {

    private FieldDescriptorList descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorList();
    }

    private DataListOptionEntity option(UUID id) {
        var opt = new DataListOptionEntity();
        opt.setId(id);
        return opt;
    }

    @Nested
    class Add {

        @Test
        void add_intoNullOptions_initializesListAndAppendsOption() {
            // null options is a documented flag meaning "long list"; add() must materialize the list lazily
            assertNull(descriptor.options(), "options starts null as the 'long list' flag");

            var opt = option(UUID.randomUUID());
            var returned = descriptor.add(opt);

            assertSame(descriptor, returned, "add must be chainable (return this)");
            assertNotNull(descriptor.options(), "add must initialize the options list");
            assertEquals(1, descriptor.options().size());
            assertSame(opt, descriptor.options().get(0));
        }

        @Test
        void add_intoInitializedList_appendsWithoutResetting() {
            descriptor.add(option(UUID.randomUUID()));

            var second = option(UUID.randomUUID());
            descriptor.add(second);

            assertEquals(2, descriptor.options().size());
            assertSame(second, descriptor.options().get(1));
        }
    }

    @Nested
    class ApplyUUIDSetIfNotEmpty {

        @Test
        void applyUUIDSetIfNotEmpty_nullSource_doesNotInvokeConsumer() {
            var invocations = new AtomicInteger();
            var returned = descriptor.applyUUIDSetIfNotEmpty(
                    null,
                    set -> invocations.incrementAndGet());

            assertSame(descriptor, returned, "must be chainable");
            assertEquals(0, invocations.get(), "null source must not be applied");
        }

        @Test
        void applyUUIDSetIfNotEmpty_emptySource_doesNotInvokeConsumer() {
            var invocations = new AtomicInteger();
            descriptor.applyUUIDSetIfNotEmpty(
                    new HashSet<>(),
                    set -> invocations.incrementAndGet());

            assertEquals(0, invocations.get(), "empty source must not be applied");
        }

        @Test
        void applyUUIDSetIfNotEmpty_nonEmptySource_invokesConsumerWithSameSet() {
            var captured = new ArrayList<Set<UUID>>();
            var source = new HashSet<UUID>();
            source.add(UUID.randomUUID());

            descriptor.applyUUIDSetIfNotEmpty(source, captured::add);

            assertEquals(1, captured.size());
            assertSame(source, captured.get(0), "consumer must receive the exact source set");
        }
    }
}

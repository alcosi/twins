package org.twins.core.unit.featurer.fieldtyper.storage;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.storage.FieldStorageService;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class FieldStorageServiceTest extends BaseUnitTest {

    private FieldStorageService newService(TwinFieldStorage... storages) {
        var service = new FieldStorageService();
        service.setFieldStorageList(List.copyOf(java.util.Arrays.asList(storages)));
        return service;
    }

    @Nested
    class GetConfig {

        @Test
        void getConfig_classRegisteredUnderItsExactClass_returnsSameBean() {
            var booleanStorage = mock(TwinFieldStorageBoolean.class);
            var simpleStorage = mock(TwinFieldStorageSimple.class);
            var service = newService(booleanStorage, simpleStorage);

            assertSame(booleanStorage, service.getConfig(TwinFieldStorageBoolean.class));
            assertSame(simpleStorage, service.getConfig(TwinFieldStorageSimple.class));
        }

        @Test
        void getConfig_unknownClass_returnsNull() {
            var service = newService(mock(TwinFieldStorageBoolean.class));

            assertNull(service.getConfig(TwinFieldStorageSimple.class));
        }

        @Test
        void getConfig_emptyRegistry_returnsNull() {
            var service = new FieldStorageService();
            service.setFieldStorageList(List.of());

            assertNull(service.getConfig(TwinFieldStorageBoolean.class));
        }

        @Test
        void getConfig_lastBeanForSameClassWins() {
            // Two beans of the exact same class: the registrar iterates the list and overwrites,
            // so the last entry wins (Map.put semantics). Encode the contract, not the order.
            var first = mock(TwinFieldStorageBoolean.class);
            var second = mock(TwinFieldStorageBoolean.class);
            var service = newService(first, second);

            assertSame(second, service.getConfig(TwinFieldStorageBoolean.class));
        }
    }
}

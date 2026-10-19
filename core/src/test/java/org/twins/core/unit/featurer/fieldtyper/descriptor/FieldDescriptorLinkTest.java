package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLink;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FieldDescriptorLinkTest extends BaseUnitTest {

    private FieldDescriptorLink descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new FieldDescriptorLink();
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Defaults {

        @Test
        void dstTwins_initializedNotNull() {
            assertNotNull(descriptor.dstTwins());
            assertTrue(descriptor.dstTwins().isEmpty());
        }

        @Test
        void multiple_defaultsToFalse() {
            assertFalse(descriptor.multiple());
        }

        @Test
        void linkId_defaultsToNull() {
            assertNull(descriptor.linkId());
        }
    }

    @Nested
    class DstTwins {

        @Test
        void dstTwins_addAccumulatesByTwinId() {
            // Kit is keyed by TwinEntity::getId; adding twins must make them retrievable by id
            var idA = UUID.randomUUID();
            var idB = UUID.randomUUID();

            descriptor.dstTwins().add(twin(idA));
            descriptor.dstTwins().add(twin(idB));

            assertEquals(2, descriptor.dstTwins().size());
        }

        @Test
        void dstTwins_addingTwinWithSameId_deduplicates() {
            var id = UUID.randomUUID();

            descriptor.dstTwins().add(twin(id));
            descriptor.dstTwins().add(twin(id));

            assertEquals(1, descriptor.dstTwins().size(), "Kit keyed by id must not duplicate same-id twins");
        }
    }
}

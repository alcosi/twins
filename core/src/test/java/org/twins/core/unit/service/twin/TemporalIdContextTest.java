package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.service.twin.TemporalIdContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TemporalIdContextTest extends BaseUnitTest {

    private TemporalIdContext ctx;
    private UUID projectUuid;
    private UUID taskUuid;

    @BeforeEach
    void setUp() {
        ctx = new TemporalIdContext();
        projectUuid = UUID.randomUUID();
        taskUuid = UUID.randomUUID();
        ctx.put("PROJECT-1", projectUuid);
        ctx.put("TASK-1", taskUuid);
        ctx.put("TASK-2", UUID.randomUUID());
    }

    @Nested
    class IsTemporalReference {

        @Test
        void isTemporalReference_validPrefix_returnsTrue() {
            assertTrue(TemporalIdContext.isTemporalReference("temporalId:PROJECT-1"));
            assertTrue(TemporalIdContext.isTemporalReference("temporalId:TASK-1"));
        }

        @Test
        void isTemporalReference_missingPrefix_returnsFalse() {
            assertFalse(TemporalIdContext.isTemporalReference("PROJECT-1"));
            assertFalse(TemporalIdContext.isTemporalReference("123e4567-e89b-12d3-a456-426614174000"));
            assertFalse(TemporalIdContext.isTemporalReference(null));
            assertFalse(TemporalIdContext.isTemporalReference(""));
        }
    }

    @Nested
    class ExtractTemporalKey {

        @Test
        void extractTemporalKey_validReference_returnsKey() throws ServiceException {
            assertEquals("PROJECT-1", TemporalIdContext.extractTemporalKey("temporalId:PROJECT-1"));
            assertEquals("TASK-2", TemporalIdContext.extractTemporalKey("temporalId:TASK-2"));
        }

        @Test
        void extractTemporalKey_emptyKey_throwsException() {
            var ex = assertThrows(ServiceException.class,
                    () -> TemporalIdContext.extractTemporalKey("temporalId:"));

            assertTrue(ex.getMessage().contains("key cannot be empty"));
        }

        @Test
        void extractTemporalKey_invalidCharacters_throwsException() {
            var ex = assertThrows(ServiceException.class,
                    () -> TemporalIdContext.extractTemporalKey("temporalId:../../../etc/passwd"));

            assertTrue(ex.getMessage().contains("invalid characters"));
        }

        @Test
        void extractTemporalKey_missingPrefix_throwsException() {
            var ex = assertThrows(ServiceException.class,
                    () -> TemporalIdContext.extractTemporalKey("PROJECT-1"));

            assertTrue(ex.getMessage().contains("Invalid temporal reference format"));
        }
    }

    @Nested
    class Put {

        @Test
        void put_newKey_resolvesCorrectly() {
            var newUuid = UUID.randomUUID();
            ctx.put("NEW-PROJECT", newUuid);

            assertEquals(newUuid, ctx.resolve("NEW-PROJECT"));
        }

        @Test
        void put_sameKey_overwritesValue() {
            var original = ctx.resolve("PROJECT-1");
            var newUuid = UUID.randomUUID();

            ctx.put("PROJECT-1", newUuid);

            assertEquals(newUuid, ctx.resolve("PROJECT-1"));
            assertNotEquals(original, ctx.resolve("PROJECT-1"));
        }
    }

    @Nested
    class Resolve {

        @Test
        void resolve_existingKey_returnsMappedUuid() {
            assertEquals(projectUuid, ctx.resolve("PROJECT-1"));
        }

        @Test
        void resolve_unknownKey_returnsNull() {
            assertNull(ctx.resolve("NONEXISTENT"));
        }

        @Test
        void resolve_nullKey_returnsNull() {
            assertNull(ctx.resolve(null));
        }
    }

    @Nested
    class Contains {

        @Test
        void contains_existingKey_returnsTrue() {
            assertTrue(ctx.contains("PROJECT-1"));
            assertTrue(ctx.contains("TASK-1"));
        }

        @Test
        void contains_unknownKey_returnsFalse() {
            assertFalse(ctx.contains("NONEXISTENT"));
        }

        @Test
        void contains_nullKey_returnsFalse() {
            assertFalse(ctx.contains(null));
        }
    }

    @Nested
    class Clear {

        @Test
        void clear_thenResolve_returnsNull() {
            ctx.clear();

            assertNull(ctx.resolve("PROJECT-1"));
            assertNull(ctx.resolve("TASK-1"));
        }

        @Test
        void clear_thenContains_returnsFalse() {
            ctx.clear();

            assertFalse(ctx.contains("PROJECT-1"));
        }
    }
}

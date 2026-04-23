package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemporalIdContextTest {

    private TemporalIdContext temporalIdContext;
    private UUID projectUuid;
    private UUID taskUuid;

    @BeforeEach
    void setUp() {
        temporalIdContext = new TemporalIdContext();
        projectUuid = UUID.randomUUID();
        taskUuid = UUID.randomUUID();
        temporalIdContext.put("PROJECT-1", projectUuid);
        temporalIdContext.put("TASK-1", taskUuid);
        temporalIdContext.put("TASK-2", UUID.randomUUID());
    }

    @Test
    void isTemporalReference_WithValidPrefix_ReturnsTrue() {
        assertTrue(TemporalIdContext.isTemporalReference("temporalId:PROJECT-1"));
        assertTrue(TemporalIdContext.isTemporalReference("temporalId:TASK-1"));
    }

    @Test
    void isTemporalReference_WithoutPrefix_ReturnsFalse() {
        assertFalse(TemporalIdContext.isTemporalReference("PROJECT-1"));
        assertFalse(TemporalIdContext.isTemporalReference("123e4567-e89b-12d3-a456-426614174000"));
        assertFalse(TemporalIdContext.isTemporalReference(null));
        assertFalse(TemporalIdContext.isTemporalReference(""));
    }

    @Test
    void extractTemporalKey_WithValidReference_ReturnsKey() throws ServiceException {
        assertEquals("PROJECT-1", TemporalIdContext.extractTemporalKey("temporalId:PROJECT-1"));
        assertEquals("TASK-2", TemporalIdContext.extractTemporalKey("temporalId:TASK-2"));
    }

    @Test
    void extractTemporalKey_WithEmptyKey_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            TemporalIdContext.extractTemporalKey("temporalId:")
        );
        assertTrue(exception.getMessage().contains("key cannot be empty"));
    }

    @Test
    void extractTemporalKey_WithInvalidCharacters_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            TemporalIdContext.extractTemporalKey("temporalId:../../../etc/passwd")
        );
        assertTrue(exception.getMessage().contains("invalid characters"));
    }

    @Test
    void extractTemporalKey_WithoutPrefix_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            TemporalIdContext.extractTemporalKey("PROJECT-1")
        );
        assertTrue(exception.getMessage().contains("Invalid temporal reference format"));
    }

    @Test
    void put_ThenResolve_ReturnsCorrectUuid() {
        UUID newUuid = UUID.randomUUID();
        temporalIdContext.put("NEW-PROJECT", newUuid);
        assertEquals(newUuid, temporalIdContext.resolve("NEW-PROJECT"));
    }

    @Test
    void resolve_WithExistingTemporalId_ReturnsMappedUuid() {
        UUID result = temporalIdContext.resolve("PROJECT-1");
        assertEquals(projectUuid, result);
    }

    @Test
    void resolve_WithNonExistentTemporalId_ReturnsNull() {
        UUID result = temporalIdContext.resolve("NONEXISTENT");
        assertNull(result);
    }

    @Test
    void resolve_WithNull_ReturnsNull() {
        UUID result = temporalIdContext.resolve(null);
        assertNull(result);
    }

    @Test
    void contains_WithExistingTemporalId_ReturnsTrue() {
        assertTrue(temporalIdContext.contains("PROJECT-1"));
        assertTrue(temporalIdContext.contains("TASK-1"));
    }

    @Test
    void contains_WithNonExistentTemporalId_ReturnsFalse() {
        assertFalse(temporalIdContext.contains("NONEXISTENT"));
    }

    @Test
    void contains_WithNull_ReturnsFalse() {
        assertFalse(temporalIdContext.contains(null));
    }

    @Test
    void clear_ThenResolve_ReturnsNull() {
        temporalIdContext.clear();
        assertNull(temporalIdContext.resolve("PROJECT-1"));
        assertNull(temporalIdContext.resolve("TASK-1"));
    }

    @Test
    void clear_ThenContains_ReturnsFalse() {
        temporalIdContext.clear();
        assertFalse(temporalIdContext.contains("PROJECT-1"));
    }

    @Test
    void put_WithSameKey_OverwritesValue() {
        UUID originalUuid = temporalIdContext.resolve("PROJECT-1");
        UUID newUuid = UUID.randomUUID();
        temporalIdContext.put("PROJECT-1", newUuid);
        assertEquals(newUuid, temporalIdContext.resolve("PROJECT-1"));
        assertNotEquals(originalUuid, temporalIdContext.resolve("PROJECT-1"));
    }
}

package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemporalIdResolverExtendedTest {

    @InjectMocks
    private TemporalIdResolver temporalIdResolver;

    private Map<String, UUID> temporalIdMap;
    private UUID projectUuid;
    private UUID taskUuid;

    @BeforeEach
    void setUp() {
        projectUuid = UUID.randomUUID();
        taskUuid = UUID.randomUUID();

        temporalIdMap = new HashMap<>();
        temporalIdMap.put("PROJECT-1", projectUuid);
        temporalIdMap.put("TASK-1", taskUuid);
        temporalIdMap.put("TASK-2", UUID.randomUUID());
    }

    @Test
    void testCompleteTemporalIdWorkflow() throws ServiceException {
        // Test basic temporalId resolution
        String temporalRef = "temporalId:PROJECT-1";
        UUID resolved = temporalIdResolver.resolveUuid(temporalRef, temporalIdMap);
        assertEquals(projectUuid, resolved);
    }

    @Test
    void testMixedUuidAndTemporalId() throws ServiceException {
        // Test regular UUID resolution
        String regularUuid = projectUuid.toString();
        UUID resolved = temporalIdResolver.resolveUuid(regularUuid, temporalIdMap);
        assertEquals(projectUuid, resolved);

        // Test temporalId resolution
        String temporalRef = "temporalId:TASK-1";
        resolved = temporalIdResolver.resolveUuid(temporalRef, temporalIdMap);
        assertEquals(taskUuid, resolved);
    }

    @Test
    void testFieldWithTemporalId() throws ServiceException {
        // Simulating field value with temporalId reference
        String fieldValue = "temporalId:PROJECT-1";
        UUID resolved = temporalIdResolver.resolveUuid(fieldValue, temporalIdMap);
        assertEquals(projectUuid, resolved);
    }

    @Test
    void testUniquenessValidation() throws ServiceException {
        List<TwinCreateRqDTOv2> twins = new ArrayList<>();
        twins.add(createTwinDto("PROJECT-1", UUID.randomUUID()));
        twins.add(createTwinDto("TASK-1", UUID.randomUUID()));
        twins.add(createTwinDto("TASK-2", UUID.randomUUID()));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniqueness(twins));
    }

    @Test
    void testDuplicateTemporalIdDetection() {
        List<TwinCreateRqDTOv2> twins = new ArrayList<>();
        twins.add(createTwinDto("PROJECT-1", UUID.randomUUID()));
        twins.add(createTwinDto("TASK-1", UUID.randomUUID()));
        twins.add(createTwinDto("PROJECT-1", UUID.randomUUID())); // duplicate

        assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdUniqueness(twins)
        );
    }

    @Test
    void testCyclicDependencyDetection() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreate("TASK-1", "temporalId:TASK-2"));
        twinCreates.add(createTwinCreate("TASK-2", "temporalId:TASK-1"));

        assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
    }

    @Test
    void testForwardReference() throws ServiceException {
        // Forward reference: referencing a twin defined later in the request
        String forwardRef = "temporalId:TASK-10";
        temporalIdMap.put("TASK-10", UUID.randomUUID());

        UUID resolved = temporalIdResolver.resolveUuid(forwardRef, temporalIdMap);
        assertNotNull(resolved);
    }

    @Test
    void testBackwardCompatibility() throws ServiceException {
        // Test that regular UUIDs still work (backward compatibility)
        String regularUuid = UUID.randomUUID().toString();
        UUID resolved = temporalIdResolver.resolveUuid(regularUuid, temporalIdMap);
        assertEquals(UUID.fromString(regularUuid), resolved);
    }

    // Helper methods
    private TwinCreateRqDTOv2 createTwinDto(String temporalId, UUID classId) {
        TwinCreateRqDTOv2 dto = new TwinCreateRqDTOv2();
        dto.setTemporalId(temporalId);
        dto.setClassId(classId);
        return dto;
    }

    private TwinCreate createTwinCreate(String temporalId, String headTwinRef) {
        TwinCreate tc = new TwinCreate();
        tc.setTemporalId(temporalId);
        tc.setHeadTwinRef(headTwinRef);
        return tc;
    }
}

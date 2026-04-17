package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemporalIdResolverTest {

    @InjectMocks
    private TemporalIdResolver temporalIdResolver;

    private Map<String, UUID> temporalIdMap;

    @BeforeEach
    void setUp() {
        temporalIdMap = new HashMap<>();
        temporalIdMap.put("PROJECT-1", UUID.randomUUID());
        temporalIdMap.put("TASK-1", UUID.randomUUID());
        temporalIdMap.put("TASK-2", UUID.randomUUID());
    }

    @Test
    void isTemporalReference_WithValidPrefix_ReturnsTrue() {
        assertTrue(temporalIdResolver.isTemporalReference("temporalId:PROJECT-1"));
        assertTrue(temporalIdResolver.isTemporalReference("temporalId:TASK-1"));
    }

    @Test
    void isTemporalReference_WithoutPrefix_ReturnsFalse() {
        assertFalse(temporalIdResolver.isTemporalReference("PROJECT-1"));
        assertFalse(temporalIdResolver.isTemporalReference("123e4567-e89b-12d3-a456-426614174000"));
        assertFalse(temporalIdResolver.isTemporalReference(null));
        assertFalse(temporalIdResolver.isTemporalReference(""));
    }

    @Test
    void extractTemporalKey_WithValidReference_ReturnsKey() throws ServiceException {
        assertEquals("PROJECT-1", temporalIdResolver.extractTemporalKey("temporalId:PROJECT-1"));
        assertEquals("TASK-2", temporalIdResolver.extractTemporalKey("temporalId:TASK-2"));
    }

    @Test
    void resolveUuid_WithTemporalId_ReturnsMappedUuid() throws ServiceException {
        UUID result = temporalIdResolver.resolveUuid("temporalId:PROJECT-1", temporalIdMap);
        assertEquals(temporalIdMap.get("PROJECT-1"), result);
    }

    @Test
    void resolveUuid_WithRegularUuid_ReturnsSameUuid() throws ServiceException {
        UUID testUuid = UUID.randomUUID();
        UUID result = temporalIdResolver.resolveUuid(testUuid.toString(), temporalIdMap);
        assertEquals(testUuid, result);
    }

    @Test
    void resolveUuid_WithInvalidTemporalId_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.resolveUuid("temporalId:MISSING", temporalIdMap)
        );
        assertTrue(exception.getMessage().contains("Temporal ID reference not found"));
    }

    @Test
    void resolveUuid_WithInvalidUuid_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.resolveUuid("invalid-uuid", temporalIdMap)
        );
        assertTrue(exception.getMessage().contains("Invalid UUID or temporal reference"));
    }

    @Test
    void resolveUuid_WithNull_ReturnsNull() throws ServiceException {
        UUID result = temporalIdResolver.resolveUuid(null, temporalIdMap);
        assertNull(result);
    }

    @Test
    void resolveUuid_WithEmptyString_ReturnsNull() throws ServiceException {
        UUID result = temporalIdResolver.resolveUuid("  ", temporalIdMap);
        assertNull(result);
    }

    @Test
    void validateTemporalIdUniqueness_WithUniqueIds_NoException() throws ServiceException {
        List<TwinCreate> twins = new ArrayList<>();
        twins.add(createTwinCreateWithTemporalId("PROJECT-1"));
        twins.add(createTwinCreateWithTemporalId("TASK-1"));
        twins.add(createTwinCreateWithTemporalId("TASK-2"));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniqueness(twins));
    }

    @Test
    void validateTemporalIdUniqueness_WithDuplicateIds_ThrowsException() {
        List<TwinCreate> twins = new ArrayList<>();
        twins.add(createTwinCreateWithTemporalId("PROJECT-1"));
        twins.add(createTwinCreateWithTemporalId("TASK-1"));
        twins.add(createTwinCreateWithTemporalId("PROJECT-1")); // duplicate

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdUniqueness(twins)
        );
        assertTrue(exception.getMessage().contains("Duplicate temporalId"));
    }

    @Test
    void validateTemporalIdUniqueness_WithNullIds_NoException() throws ServiceException {
        List<TwinCreate> twins = new ArrayList<>();
        twins.add(createTwinCreateWithTemporalId(null));
        twins.add(createTwinCreateWithTemporalId(null));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniqueness(twins));
    }

    @Test
    void validateTemporalIdUniquenessDTO_WithUniqueIds_NoException() throws ServiceException {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        dtos.add(createDtoWithTemporalId("PROJECT-1"));
        dtos.add(createDtoWithTemporalId("TASK-1"));
        dtos.add(createDtoWithTemporalId("TASK-2"));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniquenessDTO(dtos));
    }

    @Test
    void validateTemporalIdUniquenessDTO_WithDuplicateIds_ThrowsException() {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        dtos.add(createDtoWithTemporalId("PROJECT-1"));
        dtos.add(createDtoWithTemporalId("TASK-1"));
        dtos.add(createDtoWithTemporalId("PROJECT-1")); // duplicate

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdUniquenessDTO(dtos)
        );
        assertTrue(exception.getMessage().contains("Duplicate temporalId"));
    }

    @Test
    void detectCycles_WithNoCycles_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithRef("PROJECT-1", null));
        twinCreates.add(createTwinCreateWithRef("TASK-1", "temporalId:PROJECT-1"));
        twinCreates.add(createTwinCreateWithRef("TASK-2", "temporalId:PROJECT-1"));

        assertDoesNotThrow(() -> temporalIdResolver.detectCycles(twinCreates));
    }

    @Test
    void detectCycles_WithDirectCycle_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithRef("TASK-1", "temporalId:TASK-1")); // direct cycle

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void detectCycles_WithIndirectCycle_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithRef("TASK-1", "temporalId:TASK-2"));
        twinCreates.add(createTwinCreateWithRef("TASK-2", "temporalId:TASK-3"));
        twinCreates.add(createTwinCreateWithRef("TASK-3", "temporalId:TASK-1")); // cycle

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void detectCycles_WithRegularUuid_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithRef("PROJECT-1", UUID.randomUUID().toString()));
        twinCreates.add(createTwinCreateWithRef("TASK-1", "temporalId:PROJECT-1"));

        assertDoesNotThrow(() -> temporalIdResolver.detectCycles(twinCreates));
    }

    @Test
    void detectCycles_ReturnsSortedOrder() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        // TASK-3 depends on TASK-2
        twinCreates.add(createTwinCreateWithRef("TASK-3", "temporalId:TASK-2"));
        // TASK-2 depends on TASK-1
        twinCreates.add(createTwinCreateWithRef("TASK-2", "temporalId:TASK-1"));
        // TASK-1 has no dependencies
        twinCreates.add(createTwinCreateWithRef("TASK-1", null));

        List<Integer> sorted = temporalIdResolver.detectCycles(twinCreates);

        // Should return indices in order: TASK-1 (index 2), TASK-2 (index 1), TASK-3 (index 0)
        assertEquals(Arrays.asList(2, 1, 0), sorted);
    }

    @Test
    void buildTemporalIdMap_WithValidInput_ReturnsCorrectMap() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithTemporalId("PROJECT-1"));
        twinCreates.add(createTwinCreateWithTemporalId("TASK-1"));
        twinCreates.add(createTwinCreateWithTemporalId("TASK-2"));

        Map<String, UUID> createdIds = new HashMap<>();
        createdIds.put("twin_0", UUID.randomUUID());
        createdIds.put("twin_1", UUID.randomUUID());
        createdIds.put("twin_2", UUID.randomUUID());

        Map<String, UUID> result = temporalIdResolver.buildTemporalIdMap(twinCreates, createdIds);

        assertEquals(3, result.size());
        assertEquals(createdIds.get("twin_0"), result.get("PROJECT-1"));
        assertEquals(createdIds.get("twin_1"), result.get("TASK-1"));
        assertEquals(createdIds.get("twin_2"), result.get("TASK-2"));
    }

    @Test
    void buildTemporalIdMap_WithPartialTemporalIds_ReturnsPartialMap() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        twinCreates.add(createTwinCreateWithTemporalId("PROJECT-1"));
        twinCreates.add(createTwinCreateWithTemporalId(null)); // no temporalId
        twinCreates.add(createTwinCreateWithTemporalId("TASK-1"));

        Map<String, UUID> createdIds = new HashMap<>();
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        createdIds.put("twin_0", uuid1);
        createdIds.put("twin_1", uuid2);
        createdIds.put("twin_2", uuid3);

        Map<String, UUID> result = temporalIdResolver.buildTemporalIdMap(twinCreates, createdIds);

        assertEquals(2, result.size());
        assertEquals(uuid1, result.get("PROJECT-1"));
        assertEquals(uuid3, result.get("TASK-1"));
    }

    // Helper methods
    private TwinCreateRqDTOv2 createDtoWithTemporalId(String temporalId) {
        TwinCreateRqDTOv2 dto = new TwinCreateRqDTOv2();
        dto.setTemporalId(temporalId);
        dto.setClassId(UUID.randomUUID());
        return dto;
    }

    private TwinCreate createTwinCreateWithRef(String temporalId, String headTwinRef) {
        TwinCreate tc = new TwinCreate();
        tc.setTemporalId(temporalId);
        tc.setHeadTwinRef(headTwinRef);
        return tc;
    }

    private TwinCreate createTwinCreateWithTemporalId(String temporalId) {
        TwinCreate tc = new TwinCreate();
        tc.setTemporalId(temporalId);
        return tc;
    }

    // Additional tests for null safety and edge cases

    @Test
    void extractTemporalKey_WithEmptyKey_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.extractTemporalKey("temporalId:")
        );
        assertTrue(exception.getMessage().contains("key cannot be empty"));
    }

    @Test
    void extractTemporalKey_WithInvalidCharacters_ThrowsException() {
        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.extractTemporalKey("temporalId:../../../etc/passwd")
        );
        assertTrue(exception.getMessage().contains("invalid characters"));
    }

    @Test
    void detectCycles_WithSelfReference_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithRef("A", "temporalId:A"); // self-reference

        twinCreates.add(tc);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void isTemporalReference_WithNull_ReturnsFalse() {
        assertFalse(temporalIdResolver.isTemporalReference(null));
    }

    @Test
    void isTemporalReference_WithEmptyString_ReturnsFalse() {
        assertFalse(temporalIdResolver.isTemporalReference(""));
    }

    @Test
    void isTemporalReference_WithRegularString_ReturnsFalse() {
        assertFalse(temporalIdResolver.isTemporalReference("regular-uuid-string"));
    }

    // Tests for temporalId reference validation (DTO version)

    @Test
    void validateTemporalIdReferencesExistDTO_WithAllValid_NoException() throws ServiceException {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        TwinCreateRqDTOv2 dto1 = createDtoWithRef("A", "temporalId:B", UUID.randomUUID());
        dto1.setFields(Map.of("ref", "temporalId:B"));

        TwinCreateRqDTOv2 dto2 = createDtoWithRef("B", null, UUID.randomUUID());

        dtos.add(dto1);
        dtos.add(dto2);

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdReferencesExistDTO(dtos));
    }

    @Test
    void validateTemporalIdReferencesExistDTO_WithInvalidHeadTwinRef_ThrowsException() {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        TwinCreateRqDTOv2 dto = createDtoWithRef("A", "temporalId:NONEXISTENT", UUID.randomUUID());

        dtos.add(dto);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExistDTO(dtos)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void validateTemporalIdReferencesExistDTO_WithInvalidFieldRef_ThrowsException() {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        TwinCreateRqDTOv2 dto = createDtoWithRef("A", null, UUID.randomUUID());
        dto.setFields(Map.of("ref", "temporalId:NONEXISTENT"));

        dtos.add(dto);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExistDTO(dtos)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void validateTemporalIdReferencesExistDTO_WithInvalidLinkRef_ThrowsException() {
        List<TwinCreateRqDTOv2> dtos = new ArrayList<>();
        TwinCreateRqDTOv2 dto = createDtoWithRef("A", null, UUID.randomUUID());
        org.twins.core.dto.rest.link.TwinLinkAddDTOv1 link = new org.twins.core.dto.rest.link.TwinLinkAddDTOv1();
        link.setLinkId(UUID.randomUUID());
        link.setDstTwinId("temporalId:NONEXISTENT");
        dto.setLinks(List.of(link));

        dtos.add(dto);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExistDTO(dtos)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    // Helper method for DTO
    private TwinCreateRqDTOv2 createDtoWithRef(String temporalId, String headTwinId, UUID classId) {
        TwinCreateRqDTOv2 dto = new TwinCreateRqDTOv2();
        dto.setTemporalId(temporalId);
        dto.setHeadTwinId(headTwinId);
        dto.setClassId(classId);
        return dto;
    }
}

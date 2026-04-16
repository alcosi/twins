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
        List<TwinCreateRqDTOv2> twins = new ArrayList<>();
        twins.add(createTwinWithTemporalId("PROJECT-1"));
        twins.add(createTwinWithTemporalId("TASK-1"));
        twins.add(createTwinWithTemporalId("TASK-2"));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniqueness(twins));
    }

    @Test
    void validateTemporalIdUniqueness_WithDuplicateIds_ThrowsException() {
        List<TwinCreateRqDTOv2> twins = new ArrayList<>();
        twins.add(createTwinWithTemporalId("PROJECT-1"));
        twins.add(createTwinWithTemporalId("TASK-1"));
        twins.add(createTwinWithTemporalId("PROJECT-1")); // duplicate

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdUniqueness(twins)
        );
        assertTrue(exception.getMessage().contains("Duplicate temporalId"));
    }

    @Test
    void validateTemporalIdUniqueness_WithNullIds_NoException() throws ServiceException {
        List<TwinCreateRqDTOv2> twins = new ArrayList<>();
        twins.add(createTwinWithTemporalId(null));
        twins.add(createTwinWithTemporalId(null));

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdUniqueness(twins));
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
    private TwinCreateRqDTOv2 createTwinWithTemporalId(String temporalId) {
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

    // Additional tests for extended cycle detection

    @Test
    void detectCycles_WithFieldCycle_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc1 = createTwinCreateWithTemporalId("A");
        Map<String, String> fieldRefs1 = new HashMap<>();
        fieldRefs1.put("refField", "temporalId:B");
        tc1.setFieldRefs(fieldRefs1);

        TwinCreate tc2 = createTwinCreateWithTemporalId("B");
        Map<String, String> fieldRefs2 = new HashMap<>();
        fieldRefs2.put("refField", "temporalId:A");
        tc2.setFieldRefs(fieldRefs2);

        twinCreates.add(tc1);
        twinCreates.add(tc2);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void detectCycles_WithLinkCycle_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc1 = createTwinCreateWithTemporalId("A");
        List<TwinCreate.LinkRef> links1 = new ArrayList<>();
        links1.add(new TwinCreate.LinkRef().setDstTwinIdRef("temporalId:B"));
        tc1.setLinksRefList(links1);

        TwinCreate tc2 = createTwinCreateWithTemporalId("B");
        List<TwinCreate.LinkRef> links2 = new ArrayList<>();
        links2.add(new TwinCreate.LinkRef().setDstTwinIdRef("temporalId:A"));
        tc2.setLinksRefList(links2);

        twinCreates.add(tc1);
        twinCreates.add(tc2);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void detectCycles_WithMixedReferencesCycle_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();

        // A -> B (via headTwinId)
        TwinCreate tc1 = createTwinCreateWithRef("A", "temporalId:B");

        // B -> C (via field)
        TwinCreate tc2 = createTwinCreateWithTemporalId("B");
        Map<String, String> fieldRefs2 = new HashMap<>();
        fieldRefs2.put("next", "temporalId:C");
        tc2.setFieldRefs(fieldRefs2);

        // C -> A (via link)
        TwinCreate tc3 = createTwinCreateWithTemporalId("C");
        List<TwinCreate.LinkRef> links3 = new ArrayList<>();
        links3.add(new TwinCreate.LinkRef().setDstTwinIdRef("temporalId:A"));
        tc3.setLinksRefList(links3);

        twinCreates.add(tc1);
        twinCreates.add(tc2);
        twinCreates.add(tc3);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.detectCycles(twinCreates)
        );
        assertTrue(exception.getMessage().contains("Cyclic dependency"));
    }

    @Test
    void detectCycles_WithNoCyclesInFields_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();

        TwinCreate tc1 = createTwinCreateWithTemporalId("PROJECT-1");
        Map<String, String> fieldRefs1 = new HashMap<>();
        fieldRefs1.put("milestone", "temporalId:MILESTONE-1");
        tc1.setFieldRefs(fieldRefs1);

        TwinCreate tc2 = createTwinCreateWithTemporalId("MILESTONE-1");
        // No references back to PROJECT-1

        twinCreates.add(tc1);
        twinCreates.add(tc2);

        assertDoesNotThrow(() -> temporalIdResolver.detectCycles(twinCreates));
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
    void detectCycles_WithNullFieldValues_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithTemporalId("A");
        Map<String, String> fieldRefs = new HashMap<>();
        fieldRefs.put("field1", "temporalId:B");
        fieldRefs.put("field2", null); // null value should not cause NPE
        fieldRefs.put("field3", "regular-uuid-string");
        tc.setFieldRefs(fieldRefs);

        twinCreates.add(tc);
        twinCreates.add(createTwinCreateWithTemporalId("B"));

        assertDoesNotThrow(() -> temporalIdResolver.detectCycles(twinCreates));
    }

    @Test
    void detectCycles_WithNullLinkRef_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithTemporalId("A");
        List<TwinCreate.LinkRef> links = new ArrayList<>();
        links.add(new TwinCreate.LinkRef().setDstTwinIdRef("temporalId:B"));
        links.add(null); // null entry should not cause NPE
        tc.setLinksRefList(links);

        twinCreates.add(tc);
        twinCreates.add(createTwinCreateWithTemporalId("B"));

        assertDoesNotThrow(() -> temporalIdResolver.detectCycles(twinCreates));
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
    void detectCycles_WithFieldSelfReference_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithTemporalId("A");
        Map<String, String> fieldRefs = new HashMap<>();
        fieldRefs.put("selfRef", "temporalId:A"); // self-reference via field
        tc.setFieldRefs(fieldRefs);

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

    // Tests for temporalId reference validation

    @Test
    void validateTemporalIdReferencesExist_WithAllValid_NoException() throws ServiceException {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc1 = createTwinCreateWithRef("A", "temporalId:B");
        Map<String, String> fieldRefs1 = new HashMap<>();
        fieldRefs1.put("ref", "temporalId:B");
        tc1.setFieldRefs(fieldRefs1);

        TwinCreate tc2 = createTwinCreateWithTemporalId("B");

        twinCreates.add(tc1);
        twinCreates.add(tc2);

        assertDoesNotThrow(() -> temporalIdResolver.validateTemporalIdReferencesExist(twinCreates));
    }

    @Test
    void validateTemporalIdReferencesExist_WithInvalidHeadTwinRef_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithRef("A", "temporalId:NONEXISTENT");

        twinCreates.add(tc);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExist(twinCreates)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void validateTemporalIdReferencesExist_WithInvalidFieldRef_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithTemporalId("A");
        Map<String, String> fieldRefs = new HashMap<>();
        fieldRefs.put("ref", "temporalId:NONEXISTENT");
        tc.setFieldRefs(fieldRefs);

        twinCreates.add(tc);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExist(twinCreates)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void validateTemporalIdReferencesExist_WithInvalidLinkRef_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc = createTwinCreateWithTemporalId("A");
        List<TwinCreate.LinkRef> links = new ArrayList<>();
        links.add(new TwinCreate.LinkRef().setDstTwinIdRef("temporalId:NONEXISTENT"));
        tc.setLinksRefList(links);

        twinCreates.add(tc);

        ServiceException exception = assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExist(twinCreates)
        );
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void validateTemporalIdReferencesExist_WithMixedValidInvalid_ThrowsException() {
        List<TwinCreate> twinCreates = new ArrayList<>();
        TwinCreate tc1 = createTwinCreateWithRef("A", "temporalId:B");

        TwinCreate tc2 = createTwinCreateWithTemporalId("B");
        Map<String, String> fieldRefs = new HashMap<>();
        fieldRefs.put("ref", "temporalId:NONEXISTENT");
        tc2.setFieldRefs(fieldRefs);

        twinCreates.add(tc1);
        twinCreates.add(tc2);

        assertThrows(ServiceException.class, () ->
            temporalIdResolver.validateTemporalIdReferencesExist(twinCreates)
        );
    }
}

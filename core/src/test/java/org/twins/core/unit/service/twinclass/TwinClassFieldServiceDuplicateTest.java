package org.twins.core.unit.service.twinclass;

import org.twins.core.base.BaseUnitTest;
import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
class TwinClassFieldServiceDuplicateTest extends BaseUnitTest {

    @Mock private TwinClassFieldService twinClassFieldService;
    @Mock private TwinClassService twinClassService;
    @Mock private I18nService i18nService;

    private TwinClassFieldDuplicateService twinClassFieldDuplicateService;

    private TwinClassFieldEntity srcField;
    private TwinClassEntity srcClass;
    private TwinClassEntity dstClass;
    private UUID srcFieldId;
    private UUID srcClassId;
    private UUID dstClassId;
    private UUID viewPermissionId;
    private UUID editPermissionId;
    private HashMap<String, String> fieldTyperParams;
    private HashMap<String, String> twinSorterParams;
    private HashMap<String, String> fieldInitializerParams;
    private Map<String, String> externalProperties;

    @BeforeEach
    void setUp() {
        twinClassFieldDuplicateService = new TwinClassFieldDuplicateService(
                twinClassFieldService, twinClassService
        );
        twinClassFieldDuplicateService.setI18nService(i18nService);

        srcFieldId = UUID.randomUUID();
        srcClassId = UUID.randomUUID();
        dstClassId = UUID.randomUUID();
        viewPermissionId = UUID.randomUUID();
        editPermissionId = UUID.randomUUID();

        srcClass = new TwinClassEntity().setId(srcClassId);
        dstClass = new TwinClassEntity().setId(dstClassId);

        fieldTyperParams = new HashMap<>(Map.of("typer", "v1"));
        twinSorterParams = new HashMap<>(Map.of("sorter", "v2"));
        fieldInitializerParams = new HashMap<>(Map.of("init", "v3"));
        externalProperties = new HashMap<>(Map.of("ext", "v4"));

        srcField = new TwinClassFieldEntity()
                .setId(srcFieldId)
                .setKey("my_field")
                .setTwinClassId(srcClassId)
                .setTwinClass(srcClass)
                .setFieldTyperFeaturerId(1101)
                .setFieldTyperParams(fieldTyperParams)
                .setTwinSorterFeaturerId(2201)
                .setTwinSorterParams(twinSorterParams)
                .setFieldInitializerFeaturerId(3301)
                .setFieldInitializerParams(fieldInitializerParams)
                .setViewPermissionId(viewPermissionId)
                .setEditPermissionId(editPermissionId)
                .setRequired(true)
                .setExternalId("ext-id-1")
                .setExternalProperties(externalProperties)
                .setSystem(false)
                .setInheritable(true)
                .setDependentField(true)
                .setHasDependentFields(true)
                .setOrder(42)
                .setProjectionField(true)
                .setHasProjectedFields(true);
    }

    /** Builds a TwinClassFieldDuplicate with originalTwinClassField pre-set to bypass DB load. */
    private TwinClassFieldDuplicate duplicateOf(TwinClassFieldEntity original, UUID newTwinClassId, String key) {
        TwinClassFieldDuplicate d = new TwinClassFieldDuplicate();
        d.setOriginalEntityId(original.getId());
        d.setOriginalEntity(original);
        d.setNewKey(key);
        d.setNewParentEntityId(newTwinClassId);
        // bypass DB load: parent entity service is a mock, so wire the entity directly
        if (newTwinClassId != null) {
            d.setNewParentEntity(newTwinClassId.equals(dstClassId) ? dstClass : srcClass);
        }
        return d;
    }

    /** Stubs saveSafe(Collection) on the entity service to echo and capture saved entities. */
    private List<TwinClassFieldEntity> stubSaveSafeAndCapture() throws ServiceException {
        List<TwinClassFieldEntity> captured = new ArrayList<>();
        doAnswer(inv -> {
            Collection<TwinClassFieldEntity> col = inv.getArgument(0);
            captured.addAll(col);
            return col;
        }).when(twinClassFieldService).saveSafe(any(Collection.class));
        return captured;
    }

    @Nested
    class CopyPersistentFields {

        @Test
        void copiesEveryPersistentFieldAndSetsTargetClass() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertNotSame(srcField, saved);
            assertEquals(dstClassId, saved.getTwinClassId());
            assertSame(dstClass, saved.getTwinClass());

            assertEquals(srcField.getFieldTyperFeaturerId(), saved.getFieldTyperFeaturerId());
            assertEquals(fieldTyperParams, saved.getFieldTyperParams());
            assertEquals(srcField.getTwinSorterFeaturerId(), saved.getTwinSorterFeaturerId());
            assertEquals(twinSorterParams, saved.getTwinSorterParams());
            assertEquals(srcField.getFieldInitializerFeaturerId(), saved.getFieldInitializerFeaturerId());
            assertEquals(fieldInitializerParams, saved.getFieldInitializerParams());

            assertEquals(viewPermissionId, saved.getViewPermissionId());
            assertEquals(editPermissionId, saved.getEditPermissionId());
            assertEquals(Boolean.TRUE, saved.getRequired());
            assertEquals("ext-id-1", saved.getExternalId());
            assertEquals(externalProperties, saved.getExternalProperties());
            assertEquals(Boolean.FALSE, saved.getSystem());
            assertEquals(Boolean.TRUE, saved.getInheritable());
            assertEquals(Integer.valueOf(42), saved.getOrder());
        }

        @Test
        void hardcodesRelationFlagsToFalse() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertEquals(Boolean.FALSE, saved.getDependentField());
            assertEquals(Boolean.FALSE, saved.getHasDependentFields());
            assertEquals(Boolean.FALSE, saved.getProjectionField());
            assertEquals(Boolean.FALSE, saved.getHasProjectedFields());
        }

        @Test
        void throwsWhenNewTwinClassIdIsNull() {
            TwinClassFieldDuplicate dup = duplicateOf(srcField, null, "new_key"); // newTwinClassId = null

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> twinClassFieldDuplicateService.duplicate(List.of(dup)));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void lowercasesNewKey() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "MixedCaseKey")));

            assertEquals("mixedcasekey", captured.get(0).getKey());
        }
    }

    @Nested
    class I18n {

        @Test
        void duplicatesAllFourI18nReferencesWhenPresent() throws ServiceException {
            UUID srcNameI18nId = UUID.randomUUID();
            UUID srcDescI18nId = UUID.randomUUID();
            UUID srcFeValI18nId = UUID.randomUUID();
            UUID srcBeValI18nId = UUID.randomUUID();

            srcField
                    .setNameI18nId(srcNameI18nId)
                    .setDescriptionI18nId(srcDescI18nId)
                    .setFeValidationErrorI18nId(srcFeValI18nId)
                    .setBeValidationErrorI18nId(srcBeValI18nId);

            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertNotNull(saved.getNameI18nId());
            assertNotNull(saved.getDescriptionI18nId());
            assertNotNull(saved.getFeValidationErrorI18nId());
            assertNotNull(saved.getBeValidationErrorI18nId());
            assertNotEquals(srcNameI18nId, saved.getNameI18nId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18nId());
            assertNotEquals(srcFeValI18nId, saved.getFeValidationErrorI18nId());
            assertNotEquals(srcBeValI18nId, saved.getBeValidationErrorI18nId());

            // i18n copies are committed as a single batch (srcId → newId) in the pre-commit phase
            verify(i18nService).duplicateTranslations(argThat(m ->
                    m != null
                            && m.size() == 4
                            && m.containsKey(srcNameI18nId)
                            && m.containsKey(srcDescI18nId)
                            && m.containsKey(srcFeValI18nId)
                            && m.containsKey(srcBeValI18nId)
                            && saved.getNameI18nId().equals(m.get(srcNameI18nId))
                            && saved.getDescriptionI18nId().equals(m.get(srcDescI18nId))
                            && saved.getFeValidationErrorI18nId().equals(m.get(srcFeValI18nId))
                            && saved.getBeValidationErrorI18nId().equals(m.get(srcBeValI18nId))));
        }

        @Test
        void skipsI18nDuplicationForNullReferences() throws ServiceException {
            UUID srcDescI18nId = UUID.randomUUID();

            srcField
                    .setNameI18nId(null)
                    .setDescriptionI18nId(srcDescI18nId)
                    .setFeValidationErrorI18nId(null)
                    .setBeValidationErrorI18nId(null);

            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertNull(saved.getNameI18nId());
            assertNotNull(saved.getDescriptionI18nId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18nId());
            assertNull(saved.getFeValidationErrorI18nId());
            assertNull(saved.getBeValidationErrorI18nId());

            // Only one src id was reserved → remap size is 1, key = srcDescI18nId
            verify(i18nService).duplicateTranslations(argThat(m ->
                    m != null && m.size() == 1 && m.containsKey(srcDescI18nId)));
        }

        @Test
        void skipsAllI18nWhenSrcHasNone() throws ServiceException {
            stubSaveSafeAndCapture();

            twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            verify(i18nService).duplicateTranslations(argThat(m -> m == null || m.isEmpty()));
        }
    }

    @Nested
    class BatchValidation {

        @Test
        void throwsOnDuplicateNewKeyInBatch() {
            TwinClassFieldEntity src2 = new TwinClassFieldEntity()
                    .setId(UUID.randomUUID())
                    .setTwinClassId(srcClassId);

            List<TwinClassFieldDuplicate> batch = List.of(
                    duplicateOf(srcField, dstClassId, "same_key"),
                    duplicateOf(src2, dstClassId, "same_key")
            );

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> twinClassFieldDuplicateService.duplicate(batch));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void acceptsBatchWithDistinctKeys() throws ServiceException {
            TwinClassFieldEntity src2 = new TwinClassFieldEntity()
                    .setId(UUID.randomUUID())
                    .setKey("field_two")
                    .setTwinClassId(srcClassId)
                    .setTwinClass(srcClass);

            stubSaveSafeAndCapture();

            Collection<TwinClassFieldEntity> result = twinClassFieldDuplicateService.duplicate(List.of(
                    duplicateOf(srcField, dstClassId, "key_one"),
                    duplicateOf(src2, dstClassId, "key_two")
            ));

            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyOnEmptyInput() throws ServiceException {
            Collection<TwinClassFieldEntity> result = twinClassFieldDuplicateService.duplicate(Collections.emptyList());

            assertTrue(result.isEmpty());
            verify(twinClassFieldService, never()).saveSafe(any(Collection.class));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class UniquenessValidation {

        @Test
        @Disabled("TODO: implement existsByKeyAndTwinClassId check before saveSafe")
        void throwsWhenFieldWithSameKeyAlreadyExistsInTargetClass() throws ServiceException {
            assertThrows(ServiceException.class,
                    () -> twinClassFieldDuplicateService.duplicate(List.of(duplicateOf(srcField, dstClassId, "new_key"))));

            verify(twinClassFieldService, never()).saveSafe(any(Collection.class));
        }

        @Test
        @Disabled("TODO: implement existsByKeyAndTwinClassId check before saveSafe")
        void throwsForEachConflictingKeyInBatch() throws ServiceException {
            TwinClassFieldEntity src2 = new TwinClassFieldEntity()
                    .setId(UUID.randomUUID())
                    .setKey("field_two")
                    .setTwinClassId(srcClassId)
                    .setTwinClass(srcClass);

            stubSaveSafeAndCapture();

            assertThrows(ServiceException.class,
                    () -> twinClassFieldDuplicateService.duplicate(List.of(
                            duplicateOf(srcField, dstClassId, "ok_key"),
                            duplicateOf(src2, dstClassId, "conflict_key")
                    )));

            verify(twinClassFieldService, never()).saveSafe(any(Collection.class));
        }
    }
}

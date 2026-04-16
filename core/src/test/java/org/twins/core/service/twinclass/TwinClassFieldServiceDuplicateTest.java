package org.twins.core.service.twinclass;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinClassFieldServiceDuplicateTest {

    @Mock private I18nService i18nService;
    @Mock private TwinClassFieldRepository twinClassFieldRepository;
    @Mock private TwinClassService twinClassService;

    private TwinClassFieldService twinClassFieldService;

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
        twinClassFieldService = Mockito.spy(
                new TwinClassFieldService(
                        twinClassFieldRepository, i18nService, null, null,
                        null, null, twinClassService, null, null
                )
        );

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
                .setDependentField(true)
                .setHasDependentFields(true)
                .setOrder(42)
                .setProjectionField(true)
                .setHasProjectedFields(true);
    }

    /** Builds a TwinClassFieldDuplicate with originalTwinClassField pre-set to bypass DB load. */
    private TwinClassFieldDuplicate duplicateOf(TwinClassFieldEntity original, UUID newTwinClassId, String key) {
        return new TwinClassFieldDuplicate()
                .setOriginalTwinClassFieldId(original.getId())
                .setOriginalTwinClassField(original)
                .setNewTwinClassId(newTwinClassId)
                .setNewTwinClass(dstClass)
                .setNewKey(key);
    }

    /** Stubs saveSafe(Collection) to echo and capture saved entities. */
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
    class CopyPersistentFieldsTests {

        @Test
        void copiesEveryPersistentFieldAndSetsTargetClass() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key")));

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
            assertEquals(Integer.valueOf(42), saved.getOrder());
        }

        @Test
        void hardcodesRelationFlagsToFalse() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertEquals(Boolean.FALSE, saved.getDependentField());
            assertEquals(Boolean.FALSE, saved.getHasDependentFields());
            assertEquals(Boolean.FALSE, saved.getProjectionField());
            assertEquals(Boolean.FALSE, saved.getHasProjectedFields());
        }

        @Test
        void usesOriginalClassIdWhenNewTwinClassIdIsNull() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            TwinClassFieldDuplicate dup = duplicateOf(srcField, null, "new_key"); // newTwinClassId = null

            twinClassFieldService.duplicateFields(List.of(dup));

            assertEquals(srcClassId, captured.get(0).getTwinClassId());
        }

        @Test
        void lowercasesNewKey() throws ServiceException {
            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "MixedCaseKey")));

            assertEquals("mixedcasekey", captured.get(0).getKey());
        }
    }

    @Nested
    class I18nTests {

        @Test
        void duplicatesAllFourI18nReferencesWhenPresent() throws ServiceException {
            UUID srcNameI18nId = UUID.randomUUID();
            UUID srcDescI18nId = UUID.randomUUID();
            UUID srcFeValI18nId = UUID.randomUUID();
            UUID srcBeValI18nId = UUID.randomUUID();
            UUID dupNameI18nId = UUID.randomUUID();
            UUID dupDescI18nId = UUID.randomUUID();
            UUID dupFeValI18nId = UUID.randomUUID();
            UUID dupBeValI18nId = UUID.randomUUID();

            srcField
                    .setNameI18nId(srcNameI18nId)
                    .setDescriptionI18nId(srcDescI18nId)
                    .setFeValidationErrorI18nId(srcFeValI18nId)
                    .setBeValidationErrorI18nId(srcBeValI18nId);

            when(i18nService.duplicateI18n(srcNameI18nId)).thenReturn(new I18nEntity().setId(dupNameI18nId));
            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));
            when(i18nService.duplicateI18n(srcFeValI18nId)).thenReturn(new I18nEntity().setId(dupFeValI18nId));
            when(i18nService.duplicateI18n(srcBeValI18nId)).thenReturn(new I18nEntity().setId(dupBeValI18nId));

            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertEquals(dupNameI18nId, saved.getNameI18nId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18nId());
            assertEquals(dupFeValI18nId, saved.getFeValidationErrorI18nId());
            assertEquals(dupBeValI18nId, saved.getBeValidationErrorI18nId());
            assertNotEquals(srcNameI18nId, saved.getNameI18nId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18nId());
            assertNotEquals(srcFeValI18nId, saved.getFeValidationErrorI18nId());
            assertNotEquals(srcBeValI18nId, saved.getBeValidationErrorI18nId());
        }

        @Test
        void skipsI18nDuplicationForNullReferences() throws ServiceException {
            UUID srcDescI18nId = UUID.randomUUID();
            UUID dupDescI18nId = UUID.randomUUID();

            srcField
                    .setNameI18nId(null)
                    .setDescriptionI18nId(srcDescI18nId)
                    .setFeValidationErrorI18nId(null)
                    .setBeValidationErrorI18nId(null);

            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));

            List<TwinClassFieldEntity> captured = stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            TwinClassFieldEntity saved = captured.get(0);
            assertNull(saved.getNameI18nId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18nId());
            assertNull(saved.getFeValidationErrorI18nId());
            assertNull(saved.getBeValidationErrorI18nId());
            verify(i18nService, times(1)).duplicateI18n(any(UUID.class));
        }

        @Test
        void skipsAllI18nWhenSrcHasNone() throws ServiceException {
            stubSaveSafeAndCapture();

            twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key")));

            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class BatchValidationTests {

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
                    () -> twinClassFieldService.duplicateFields(batch));

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

            Collection<TwinClassFieldEntity> result = twinClassFieldService.duplicateFields(List.of(
                    duplicateOf(srcField, dstClassId, "key_one"),
                    duplicateOf(src2, dstClassId, "key_two")
            ));

            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyOnEmptyInput() throws ServiceException {
            Collection<TwinClassFieldEntity> result = twinClassFieldService.duplicateFields(Collections.emptyList());

            assertTrue(result.isEmpty());
            verify(twinClassFieldService, never()).saveSafe(any(Collection.class));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class UniquenessValidationTests {

        @Test
        @Disabled("TODO: implement existsByKeyAndTwinClassId check before saveSafe")
        void throwsWhenFieldWithSameKeyAlreadyExistsInTargetClass() throws ServiceException {
            when(twinClassFieldRepository.existsByKeyAndTwinClassId("new_key", dstClassId)).thenReturn(true);

            assertThrows(ServiceException.class,
                    () -> twinClassFieldService.duplicateFields(List.of(duplicateOf(srcField, dstClassId, "new_key"))));

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

            when(twinClassFieldRepository.existsByKeyAndTwinClassId("conflict_key", dstClassId)).thenReturn(true);

            assertThrows(ServiceException.class,
                    () -> twinClassFieldService.duplicateFields(List.of(
                            duplicateOf(srcField, dstClassId, "ok_key"),
                            duplicateOf(src2, dstClassId, "conflict_key")
                    )));

            verify(twinClassFieldService, never()).saveSafe(any(Collection.class));
        }
    }
}

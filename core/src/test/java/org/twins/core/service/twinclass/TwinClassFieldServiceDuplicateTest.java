package org.twins.core.service.twinclass;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinClassFieldServiceDuplicateTest {

    @Mock
    private I18nService i18nService;

    private TwinClassFieldService twinClassFieldService;

    private TwinClassFieldEntity srcField;
    private TwinClassEntity srcClass;
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
                        null, i18nService, null, null,
                        null, null, null, null, null
                )
        );

        srcFieldId = UUID.randomUUID();
        srcClassId = UUID.randomUUID();
        dstClassId = UUID.randomUUID();
        viewPermissionId = UUID.randomUUID();
        editPermissionId = UUID.randomUUID();

        srcClass = new TwinClassEntity();
        srcClass.setId(srcClassId);

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
                .setHasDependentFields(false)
                .setOrder(42)
                .setProjectionField(false)
                .setHasProjectedFields(true);
    }

    private void stubSaveSafeEcho() throws ServiceException {
        doAnswer(inv -> inv.getArgument(0))
                .when(twinClassFieldService).saveSafe(any(TwinClassFieldEntity.class));
    }

    private TwinClassFieldEntity captureSaved() throws ServiceException {
        ArgumentCaptor<TwinClassFieldEntity> captor = ArgumentCaptor.forClass(TwinClassFieldEntity.class);
        verify(twinClassFieldService).saveSafe(captor.capture());
        return captor.getValue();
    }

    @Nested
    class DuplicateFieldForTargetClassTests {

        @Test
        void copiesEveryPersistentFieldAndSwitchesTargetClass() throws ServiceException {
            stubSaveSafeEcho();

            twinClassFieldService.duplicateField(srcField, dstClassId);

            TwinClassFieldEntity saved = captureSaved();
            assertNotSame(srcField, saved);
            assertEquals(dstClassId, saved.getTwinClassId());
            assertSame(srcClass, saved.getTwinClass());
            assertEquals("my_field", saved.getKey());

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
            assertEquals(Boolean.TRUE, saved.getDependentField());
            assertEquals(Boolean.FALSE, saved.getHasDependentFields());
            assertEquals(Integer.valueOf(42), saved.getOrder());
            assertEquals(Boolean.FALSE, saved.getProjectionField());
            assertEquals(Boolean.TRUE, saved.getHasProjectedFields());

            assertNull(saved.getNameI18nId());
            assertNull(saved.getDescriptionI18nId());
            assertNull(saved.getFeValidationErrorI18nId());
            assertNull(saved.getBeValidationErrorI18nId());
            verifyNoInteractions(i18nService);
        }

        @Test
        void lowercasesUppercaseKey() throws ServiceException {
            srcField.setKey("UPPER_KEY");
            stubSaveSafeEcho();

            twinClassFieldService.duplicateField(srcField, dstClassId);

            assertEquals("upper_key", captureSaved().getKey());
        }

        @Test
        void duplicatesAllI18nReferencesWhenPresent() throws ServiceException {
            UUID srcNameI18nId = UUID.randomUUID();
            UUID srcDescI18nId = UUID.randomUUID();
            UUID srcFeValI18nId = UUID.randomUUID();
            UUID srcBeValI18nId = UUID.randomUUID();
            UUID dupNameI18nId = UUID.randomUUID();
            UUID dupDescI18nId = UUID.randomUUID();
            UUID dupFeValI18nId = UUID.randomUUID();
            UUID dupBeValI18nId = UUID.randomUUID();

            srcField.setNameI18nId(srcNameI18nId);
            srcField.setDescriptionI18nId(srcDescI18nId);
            srcField.setFeValidationErrorI18nId(srcFeValI18nId);
            srcField.setBeValidationErrorI18nId(srcBeValI18nId);

            when(i18nService.duplicateI18n(srcNameI18nId)).thenReturn(new I18nEntity().setId(dupNameI18nId));
            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));
            when(i18nService.duplicateI18n(srcFeValI18nId)).thenReturn(new I18nEntity().setId(dupFeValI18nId));
            when(i18nService.duplicateI18n(srcBeValI18nId)).thenReturn(new I18nEntity().setId(dupBeValI18nId));
            stubSaveSafeEcho();

            twinClassFieldService.duplicateField(srcField, dstClassId);

            TwinClassFieldEntity saved = captureSaved();
            assertEquals(dupNameI18nId, saved.getNameI18nId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18nId());
            assertEquals(dupFeValI18nId, saved.getFeValidationErrorI18nId());
            assertEquals(dupBeValI18nId, saved.getBeValidationErrorI18nId());

            // source ids must not leak into the duplicate
            assertNotEquals(srcNameI18nId, saved.getNameI18nId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18nId());
            assertNotEquals(srcFeValI18nId, saved.getFeValidationErrorI18nId());
            assertNotEquals(srcBeValI18nId, saved.getBeValidationErrorI18nId());
        }

        @Test
        void skipsI18nDuplicationForNullReferences() throws ServiceException {
            UUID srcDescI18nId = UUID.randomUUID();
            UUID dupDescI18nId = UUID.randomUUID();
            srcField.setNameI18nId(null);
            srcField.setDescriptionI18nId(srcDescI18nId);
            srcField.setFeValidationErrorI18nId(null);
            srcField.setBeValidationErrorI18nId(null);

            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));
            stubSaveSafeEcho();

            twinClassFieldService.duplicateField(srcField, dstClassId);

            TwinClassFieldEntity saved = captureSaved();
            assertNull(saved.getNameI18nId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18nId());
            assertNull(saved.getFeValidationErrorI18nId());
            assertNull(saved.getBeValidationErrorI18nId());
            verify(i18nService, times(1)).duplicateI18n(any(UUID.class));
            verify(i18nService).duplicateI18n(srcDescI18nId);
        }

        @Test
        void throwsWhenSrcKeyIsNull() throws ServiceException {
            srcField.setKey(null);

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> twinClassFieldService.duplicateField(srcField, dstClassId));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT.getCode(), ex.getErrorCode());
            verify(twinClassFieldService, never()).saveSafe(any(TwinClassFieldEntity.class));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class DuplicateFieldWithNewKeyTests {

        @Test
        void duplicatesWithinSameClassUsingNewKey() throws ServiceException {
            doReturn(srcField).when(twinClassFieldService).findEntity(
                    eq(srcFieldId),
                    eq(EntitySmartService.FindMode.ifEmptyThrows),
                    eq(EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
            stubSaveSafeEcho();

            TwinClassFieldEntity result = twinClassFieldService.duplicateField(srcFieldId, "another_key");

            assertNotNull(result);
            TwinClassFieldEntity dup = captureSaved();
            assertSame(dup, result);

            assertEquals("another_key", dup.getKey());
            assertEquals(srcClassId, dup.getTwinClassId(), "new field must stay in the same class");
            assertSame(srcClass, dup.getTwinClass());
            assertEquals(srcField.getFieldTyperFeaturerId(), dup.getFieldTyperFeaturerId());
            assertEquals(srcField.getTwinSorterFeaturerId(), dup.getTwinSorterFeaturerId());
            assertEquals(srcField.getFieldInitializerFeaturerId(), dup.getFieldInitializerFeaturerId());
            assertEquals(viewPermissionId, dup.getViewPermissionId());
            assertEquals(editPermissionId, dup.getEditPermissionId());
            assertEquals(Boolean.TRUE, dup.getRequired());
            assertEquals("ext-id-1", dup.getExternalId());
            assertEquals(externalProperties, dup.getExternalProperties());
            assertEquals(Integer.valueOf(42), dup.getOrder());
        }

        @Test
        void lowercasesNewKey() throws ServiceException {
            doReturn(srcField).when(twinClassFieldService).findEntity(
                    eq(srcFieldId), any(EntitySmartService.FindMode.class),
                    any(EntitySmartService.ReadPermissionCheckMode.class));
            stubSaveSafeEcho();

            twinClassFieldService.duplicateField(srcFieldId, "MixedCaseKey");

            assertEquals("mixedcasekey", captureSaved().getKey());
        }

        @Test
        void duplicatesI18nWhenSrcHasIt() throws ServiceException {
            UUID srcNameI18nId = UUID.randomUUID();
            UUID dupNameI18nId = UUID.randomUUID();
            srcField.setNameI18nId(srcNameI18nId);

            doReturn(srcField).when(twinClassFieldService).findEntity(
                    eq(srcFieldId), any(EntitySmartService.FindMode.class),
                    any(EntitySmartService.ReadPermissionCheckMode.class));
            when(i18nService.duplicateI18n(srcNameI18nId)).thenReturn(new I18nEntity().setId(dupNameI18nId));
            stubSaveSafeEcho();

            TwinClassFieldEntity result = twinClassFieldService.duplicateField(srcFieldId, "new_key");

            assertEquals(dupNameI18nId, result.getNameI18nId());
            assertNull(result.getDescriptionI18nId());
            verify(i18nService).duplicateI18n(srcNameI18nId);
        }

        @Test
        void propagatesFindEntityException() throws ServiceException {
            ServiceException boom = new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT, "not found");
            doThrow(boom).when(twinClassFieldService).findEntity(
                    eq(srcFieldId), any(EntitySmartService.FindMode.class),
                    any(EntitySmartService.ReadPermissionCheckMode.class));

            assertThrows(ServiceException.class,
                    () -> twinClassFieldService.duplicateField(srcFieldId, "some_key"));

            verify(twinClassFieldService, never()).saveSafe(any(TwinClassFieldEntity.class));
            verifyNoInteractions(i18nService);
        }

        @Test
        void throwsWhenNewKeyIsInvalid() throws ServiceException {
            doReturn(srcField).when(twinClassFieldService).findEntity(
                    eq(srcFieldId), any(EntitySmartService.FindMode.class),
                    any(EntitySmartService.ReadPermissionCheckMode.class));

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> twinClassFieldService.duplicateField(srcFieldId, "a")); // too short

            assertEquals(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT.getCode(), ex.getErrorCode());
            verify(twinClassFieldService, never()).saveSafe(any(TwinClassFieldEntity.class));
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class DuplicateFieldsForClassTests {

        @Test
        void duplicatesEveryFieldIntoTargetClass() throws ServiceException {
            TwinClassFieldEntity f1 = new TwinClassFieldEntity()
                    .setId(UUID.randomUUID())
                    .setKey("field_one")
                    .setTwinClassId(srcClassId)
                    .setTwinClass(srcClass)
                    .setFieldTyperFeaturerId(1101);
            TwinClassFieldEntity f2 = new TwinClassFieldEntity()
                    .setId(UUID.randomUUID())
                    .setKey("field_two")
                    .setTwinClassId(srcClassId)
                    .setTwinClass(srcClass)
                    .setFieldTyperFeaturerId(1102);

            doReturn(List.of(f1, f2)).when(twinClassFieldService).findTwinClassFields(srcClassId);
            stubSaveSafeEcho();

            twinClassFieldService.duplicateFieldsForClass(null, srcClassId, dstClassId);

            ArgumentCaptor<TwinClassFieldEntity> captor = ArgumentCaptor.forClass(TwinClassFieldEntity.class);
            verify(twinClassFieldService, times(2)).saveSafe(captor.capture());

            List<TwinClassFieldEntity> saved = captor.getAllValues();
            assertEquals(2, saved.size());
            assertTrue(saved.stream().allMatch(f -> dstClassId.equals(f.getTwinClassId())));
            assertTrue(saved.stream().anyMatch(f -> "field_one".equals(f.getKey())));
            assertTrue(saved.stream().anyMatch(f -> "field_two".equals(f.getKey())));
        }

        @Test
        void doesNothingWhenSourceClassHasNoFields() throws ServiceException {
            doReturn(List.of()).when(twinClassFieldService).findTwinClassFields(srcClassId);

            twinClassFieldService.duplicateFieldsForClass(null, srcClassId, dstClassId);

            verify(twinClassFieldService, never()).saveSafe(any(TwinClassFieldEntity.class));
            verifyNoInteractions(i18nService);
        }
    }
}

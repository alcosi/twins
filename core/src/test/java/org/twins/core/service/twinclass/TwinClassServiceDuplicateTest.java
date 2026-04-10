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
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinClassServiceDuplicateTest {

    @Mock
    private TwinClassRepository twinClassRepository;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private EntitySmartService entitySmartService;

    @Mock
    private I18nService i18nService;

    @Mock
    private ApiUser apiUser;

    private TwinClassService twinClassService;

    private TwinClassEntity srcClass;
    private UUID srcClassId;
    private UUID apiUserId;
    private String newKey;

    @BeforeEach
    void setUp() {
        twinClassService = Mockito.spy(
                new TwinClassService(
                        null,
                        twinClassRepository,
                        null,
                        null,
                        twinClassFieldService,
                        null,
                        entitySmartService,
                        i18nService,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        srcClassId = UUID.randomUUID();
        apiUserId = UUID.randomUUID();
        newKey = "duplicate_key";

        srcClass = new TwinClassEntity();
        srcClass.setId(srcClassId);
        srcClass.setKey("original_key");
        srcClass.setDomainId(UUID.randomUUID());
    }

    private void stubApiUserReturnsUser() throws ServiceException {
        UserEntity user = new UserEntity().setId(apiUserId);
        when(apiUser.getUser()).thenReturn(user);
    }

    private void stubFindEntityReturnsSrc() throws ServiceException {
        doReturn(srcClass).when(twinClassService).findEntity(
                eq(srcClassId),
                eq(EntitySmartService.FindMode.ifEmptyThrows),
                eq(EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
    }

    private void stubSaveAssignsIdAndEchoes() throws ServiceException {
        doAnswer(inv -> {
            TwinClassEntity e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId(UUID.randomUUID());
            }
            return e;
        }).when(twinClassService).saveSafe(any(TwinClassEntity.class));
    }

    private TwinClassEntity captureSaved() throws ServiceException {
        ArgumentCaptor<TwinClassEntity> captor = ArgumentCaptor.forClass(TwinClassEntity.class);
        verify(twinClassService).saveSafe(captor.capture());
        return captor.getValue();
    }

    @Nested
    class CopyPersistentFieldsTests {

        @Test
        void copiesEveryPersistentFieldIntoDuplicate() throws ServiceException {
            UUID viewPermissionId = UUID.randomUUID();
            UUID createPermissionId = UUID.randomUUID();
            UUID editPermissionId = UUID.randomUUID();
            UUID deletePermissionId = UUID.randomUUID();
            UUID markerDataListId = UUID.randomUUID();
            UUID tagDataListId = UUID.randomUUID();
            UUID pageFaceId = UUID.randomUUID();
            UUID breadCrumbsFaceId = UUID.randomUUID();
            UUID generalAttRestrictId = UUID.randomUUID();
            UUID commentAttRestrictId = UUID.randomUUID();
            UUID extendsTwinClassId = UUID.randomUUID();
            UUID headTwinClassId = UUID.randomUUID();
            UUID iconLightResourceId = UUID.randomUUID();
            UUID iconDarkResourceId = UUID.randomUUID();
            HashMap<String, String> headHunterParams = new HashMap<>(Map.of("hh", "v1"));
            HashMap<String, String> externalProperties = new HashMap<>(Map.of("ext", "v2"));
            HashMap<String, Object> externalJson = new HashMap<>(Map.of("j", 42));

            srcClass.setPermissionSchemaSpace(true)
                    .setTwinflowSchemaSpace(false)
                    .setTwinClassSchemaSpace(true)
                    .setAliasSpace(false)
                    .setAssigneeRequired(true)
                    .setAbstractt(false)
                    .setUniqueName(true)
                    .setExtendsTwinClassId(extendsTwinClassId)
                    .setHeadTwinClassId(headTwinClassId)
                    .setIconDarkResourceId(iconDarkResourceId)
                    .setIconLightResourceId(iconLightResourceId)
                    .setOwnerType(OwnerType.USER)
                    .setViewPermissionId(viewPermissionId)
                    .setCreatePermissionId(createPermissionId)
                    .setEditPermissionId(editPermissionId)
                    .setDeletePermissionId(deletePermissionId)
                    .setSegment(true)
                    .setHasSegment(false)
                    .setMarkerDataListId(markerDataListId)
                    .setTagDataListId(tagDataListId)
                    .setHeadHunterFeaturerId(1701)
                    .setHeadHunterParams(headHunterParams)
                    .setHasDynamicMarkers(true)
                    .setPageFaceId(pageFaceId)
                    .setBreadCrumbsFaceId(breadCrumbsFaceId)
                    .setGeneralAttachmentRestrictionId(generalAttRestrictId)
                    .setCommentAttachmentRestrictionId(commentAttRestrictId)
                    .setExternalId("ext-cls-1")
                    .setExternalProperties(externalProperties)
                    .setExternalJson(externalJson);

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            stubSaveAssignsIdAndEchoes();

            twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            TwinClassEntity saved = captureSaved();
            assertNotSame(srcClass, saved);
            assertEquals(newKey, saved.getKey());
            assertEquals(apiUserId, saved.getCreatedByUserId());
            assertNotNull(saved.getCreatedAt());
            assertEquals(srcClass.getDomainId(), saved.getDomainId());

            assertEquals(Boolean.TRUE, saved.getPermissionSchemaSpace());
            assertEquals(Boolean.FALSE, saved.getTwinflowSchemaSpace());
            assertEquals(Boolean.TRUE, saved.getTwinClassSchemaSpace());
            assertEquals(Boolean.FALSE, saved.getAliasSpace());
            assertEquals(Boolean.TRUE, saved.getAssigneeRequired());
            assertEquals(Boolean.FALSE, saved.getAbstractt());
            assertEquals(Boolean.TRUE, saved.getUniqueName());
            assertEquals(extendsTwinClassId, saved.getExtendsTwinClassId());
            assertEquals(headTwinClassId, saved.getHeadTwinClassId());
            assertEquals(iconDarkResourceId, saved.getIconDarkResourceId());
            assertEquals(iconLightResourceId, saved.getIconLightResourceId());
            assertEquals(OwnerType.USER, saved.getOwnerType());

            assertEquals(viewPermissionId, saved.getViewPermissionId());
            assertEquals(createPermissionId, saved.getCreatePermissionId());
            assertEquals(editPermissionId, saved.getEditPermissionId());
            assertEquals(deletePermissionId, saved.getDeletePermissionId());

            assertEquals(Boolean.TRUE, saved.getSegment());
            assertEquals(Boolean.FALSE, saved.getHasSegment());
            assertEquals(markerDataListId, saved.getMarkerDataListId());
            assertEquals(tagDataListId, saved.getTagDataListId());
            assertEquals(Integer.valueOf(1701), saved.getHeadHunterFeaturerId());
            assertEquals(headHunterParams, saved.getHeadHunterParams());
            assertEquals(Boolean.TRUE, saved.getHasDynamicMarkers());
            assertEquals(pageFaceId, saved.getPageFaceId());
            assertEquals(breadCrumbsFaceId, saved.getBreadCrumbsFaceId());
            assertEquals(generalAttRestrictId, saved.getGeneralAttachmentRestrictionId());
            assertEquals(commentAttRestrictId, saved.getCommentAttachmentRestrictionId());
            assertEquals("ext-cls-1", saved.getExternalId());
            assertEquals(externalProperties, saved.getExternalProperties());
            assertEquals(externalJson, saved.getExternalJson());
        }

        @Test
        void doesNotCopyTwinClassFreezeId() throws ServiceException {
            srcClass.setTwinClassFreezeId(UUID.randomUUID());

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            stubSaveAssignsIdAndEchoes();

            twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            assertNull(captureSaved().getTwinClassFreezeId());
        }

        @Test
        void resetsCountersToZeroRegardlessOfSource() throws ServiceException {
            srcClass.setDomainAliasCounter(17)
                    .setTwinCounter(42)
                    .setHeadHierarchyCounterDirectChildren(3)
                    .setExtendsHierarchyCounterDirectChildren(5);

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            stubSaveAssignsIdAndEchoes();

            twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            TwinClassEntity saved = captureSaved();
            assertEquals(0, saved.getDomainAliasCounter());
            assertEquals(Integer.valueOf(0), saved.getTwinCounter());
            assertEquals(Integer.valueOf(0), saved.getHeadHierarchyCounterDirectChildren());
            assertEquals(Integer.valueOf(0), saved.getExtendsHierarchyCounterDirectChildren());
        }
    }

    @Nested
    class I18nTests {

        @Test
        void duplicatesBothI18nReferencesWhenPresent() throws ServiceException {
            UUID srcNameI18nId = UUID.randomUUID();
            UUID srcDescI18nId = UUID.randomUUID();
            UUID dupNameI18nId = UUID.randomUUID();
            UUID dupDescI18nId = UUID.randomUUID();

            srcClass.setNameI18NId(srcNameI18nId);
            srcClass.setDescriptionI18NId(srcDescI18nId);

            when(i18nService.duplicateI18n(srcNameI18nId)).thenReturn(new I18nEntity().setId(dupNameI18nId));
            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            stubSaveAssignsIdAndEchoes();

            twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            TwinClassEntity saved = captureSaved();
            assertEquals(dupNameI18nId, saved.getNameI18NId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18NId());
            assertNotEquals(srcNameI18nId, saved.getNameI18NId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18NId());
        }

        @Test
        void skipsI18nWhenSrcHasNone() throws ServiceException {
            srcClass.setNameI18NId(null);
            srcClass.setDescriptionI18NId(null);

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            stubSaveAssignsIdAndEchoes();

            twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            TwinClassEntity saved = captureSaved();
            assertNull(saved.getNameI18NId());
            assertNull(saved.getDescriptionI18NId());
            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class FieldDelegationTests {

        @Test
        void delegatesFieldDuplicationWithPostSaveId() throws ServiceException {
            UUID newClassId = UUID.randomUUID();

            stubApiUserReturnsUser();
            stubFindEntityReturnsSrc();
            doAnswer(inv -> {
                TwinClassEntity e = inv.getArgument(0);
                e.setId(newClassId);
                return e;
            }).when(twinClassService).saveSafe(any(TwinClassEntity.class));

            TwinClassEntity result = twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey);

            assertEquals(newClassId, result.getId());
            verify(twinClassFieldService, times(1))
                    .duplicateFieldsForClass(eq(srcClassId), eq(newClassId), eq(newKey));
        }
    }

    @Nested
    class ErrorPropagationTests {

        @Test
        void propagatesFindEntityException() throws ServiceException {
            ServiceException boom = new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "not found");
            doThrow(boom).when(twinClassService).findEntity(
                    eq(srcClassId),
                    eq(EntitySmartService.FindMode.ifEmptyThrows),
                    eq(EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));

            assertThrows(ServiceException.class,
                    () -> twinClassService.duplicateTwinClass(apiUser, srcClassId, newKey));

            verify(twinClassService, never()).saveSafe(any(TwinClassEntity.class));
            verifyNoInteractions(i18nService);
            verifyNoInteractions(twinClassFieldService);
        }
    }
}

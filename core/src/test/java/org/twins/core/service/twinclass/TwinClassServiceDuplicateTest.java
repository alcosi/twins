package org.twins.core.service.twinclass;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.twinclass.TwinClassDuplicate;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinStatusDuplicateService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinClassServiceDuplicateTest {

    @Mock private TwinClassService twinClassService;
    @Mock private TwinClassFieldDuplicateService twinClassFieldDuplicateService;
    @Mock private TwinStatusDuplicateService twinStatusDuplicateService;
    @Mock private I18nService i18nService;
    @Mock private AuthService authService;
    @Mock private ApiUser apiUser;

    private TwinClassDuplicateService twinClassDuplicateService;

    private TwinClassEntity srcClass;
    private UUID srcClassId;
    private UUID domainId;
    private UUID userId;
    private String newKey;

    @BeforeEach
    void setUp() throws ServiceException {
        twinClassDuplicateService = new TwinClassDuplicateService(
                twinClassService, twinClassFieldDuplicateService, twinStatusDuplicateService,
                i18nService, authService
        );

        srcClassId = UUID.randomUUID();
        domainId = UUID.randomUUID();
        userId = UUID.randomUUID();
        newKey = "DUPLICATE_KEY";

        srcClass = new TwinClassEntity();
        srcClass.setId(srcClassId);
        srcClass.setKey("original_key");
        srcClass.setDomainId(domainId);

        lenient().when(authService.getApiUser()).thenReturn(apiUser);
        lenient().when(apiUser.getDomainId()).thenReturn(domainId);
        lenient().when(apiUser.getUser()).thenReturn(new UserEntity().setId(userId));
    }

    /** Builds a TwinClassDuplicate with originalTwinClass pre-set to bypass DB load. */
    private TwinClassDuplicate duplicateOf(TwinClassEntity original, String key, boolean duplicateFields) {
        TwinClassDuplicate d = new TwinClassDuplicate();
        d.setOriginalEntityId(original.getId());
        d.setOriginalEntity(original);
        d.setNewKey(key);
        d.setDuplicateFields(duplicateFields);
        return d;
    }

    /** Stubs saveSafe(Collection) on the entity service to echo back the input and capture saved entities. */
    private List<TwinClassEntity> stubSaveSafeAndCapture() throws ServiceException {
        List<TwinClassEntity> captured = new ArrayList<>();
        doAnswer(inv -> {
            Collection<TwinClassEntity> col = inv.getArgument(0);
            captured.addAll(col);
            return col;
        }).when(twinClassService).saveSafe(any(Collection.class));
        return captured;
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

            srcClass
                    .setPermissionSchemaSpace(true)
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
                    .setSegment(true)
                    .setMarkerDataListId(markerDataListId)
                    .setTagDataListId(tagDataListId)
                    .setHeadHunterFeaturerId(1701)
                    .setHeadHunterParams(headHunterParams)
                    .setPageFaceId(pageFaceId)
                    .setBreadCrumbsFaceId(breadCrumbsFaceId)
                    .setGeneralAttachmentRestrictionId(generalAttRestrictId)
                    .setCommentAttachmentRestrictionId(commentAttRestrictId)
                    .setExternalId("ext-cls-1")
                    .setExternalProperties(externalProperties)
                    .setExternalJson(externalJson);

            List<TwinClassEntity> captured = stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            TwinClassEntity saved = captured.get(0);
            assertNotSame(srcClass, saved);
            assertEquals(newKey, saved.getKey());
            assertEquals(userId, saved.getCreatedByUserId());
            assertNotNull(saved.getCreatedAt());
            assertEquals(domainId, saved.getDomainId());

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
            assertEquals(Boolean.TRUE, saved.getSegment());
            assertEquals(markerDataListId, saved.getMarkerDataListId());
            assertEquals(tagDataListId, saved.getTagDataListId());
            assertEquals(Integer.valueOf(1701), saved.getHeadHunterFeaturerId());
            assertEquals(headHunterParams, saved.getHeadHunterParams());
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

            List<TwinClassEntity> captured = stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            assertNull(captured.get(0).getTwinClassFreezeId());
        }

        @Test
        void resetsCountersToZeroRegardlessOfSource() throws ServiceException {
            srcClass
                    .setTwinCounter(42)
                    .setHeadHierarchyCounterDirectChildren(3)
                    .setExtendsHierarchyCounterDirectChildren(5);

            List<TwinClassEntity> captured = stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            TwinClassEntity saved = captured.get(0);
            assertEquals(Integer.valueOf(0), saved.getTwinCounter());
            assertEquals(Integer.valueOf(0), saved.getHeadHierarchyCounterDirectChildren());
            assertEquals(Integer.valueOf(0), saved.getExtendsHierarchyCounterDirectChildren());
        }

        @Test
        void hardcodesHasSegmentAndHasDynamicMarkersToFalse() throws ServiceException {
            srcClass.setHasSegment(true).setHasDynamicMarkers(true);

            List<TwinClassEntity> captured = stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            TwinClassEntity saved = captured.get(0);
            assertEquals(Boolean.FALSE, saved.getHasSegment());
            assertEquals(Boolean.FALSE, saved.getHasDynamicMarkers());
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

            srcClass.setNameI18NId(srcNameI18nId).setDescriptionI18NId(srcDescI18nId);

            when(i18nService.duplicateI18n(srcNameI18nId)).thenReturn(new I18nEntity().setId(dupNameI18nId));
            when(i18nService.duplicateI18n(srcDescI18nId)).thenReturn(new I18nEntity().setId(dupDescI18nId));

            List<TwinClassEntity> captured = stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            TwinClassEntity saved = captured.get(0);
            assertEquals(dupNameI18nId, saved.getNameI18NId());
            assertEquals(dupDescI18nId, saved.getDescriptionI18NId());
            assertNotEquals(srcNameI18nId, saved.getNameI18NId());
            assertNotEquals(srcDescI18nId, saved.getDescriptionI18NId());
        }

        @Test
        void skipsI18nWhenSrcHasNone() throws ServiceException {
            srcClass.setNameI18NId(null).setDescriptionI18NId(null);

            stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            verifyNoInteractions(i18nService);
        }
    }

    @Nested
    class FieldDelegationTests {

        @Test
        void delegatesFieldDuplicationWhenFlagIsTrue() throws ServiceException {
            stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, true)));

            verify(twinClassFieldDuplicateService, times(1))
                    .collectViaParentMap(any(EntityDuplicateCollector.class),
                            argThat(map -> map != null && map.containsKey(srcClass)));
        }

        @Test
        void doesNotDelegateFieldsWhenFlagIsFalse() throws ServiceException {
            stubSaveSafeAndCapture();

            twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false)));

            verify(twinClassFieldDuplicateService, never())
                    .collectViaParentMap(any(), any());
        }
    }

    @Nested
    class BatchValidationTests {

        @Test
        void throwsOnDuplicateNewKeyInBatch() {
            TwinClassEntity src2 = new TwinClassEntity().setId(UUID.randomUUID()).setDomainId(domainId);

            List<TwinClassDuplicate> batch = List.of(
                    duplicateOf(srcClass, newKey, false),
                    duplicateOf(src2, newKey, false)  // same key
            );

            ServiceException ex = assertThrows(ServiceException.class,
                    () -> twinClassDuplicateService.duplicate(batch));

            assertEquals(ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE.getCode(), ex.getErrorCode());
        }

        @Test
        void acceptsBatchWithDistinctKeys() throws ServiceException {
            TwinClassEntity src2 = new TwinClassEntity().setId(UUID.randomUUID()).setDomainId(domainId);

            stubSaveSafeAndCapture();

            List<TwinClassEntity> result = new ArrayList<>(twinClassDuplicateService.duplicate(List.of(
                    duplicateOf(srcClass, "KEY_ONE", false),
                    duplicateOf(src2, "KEY_TWO", false)
            )));

            assertEquals(2, result.size());
        }
    }

    @Nested
    class UniquenessValidationTests {

        @Test
        @Disabled("TODO: implement existsById/existsByDomainIdAndKey check before saveSafe")
        void throwsWhenClassWithSameKeyAlreadyExistsInDomain() throws ServiceException {
            assertThrows(ServiceException.class,
                    () -> twinClassDuplicateService.duplicate(List.of(duplicateOf(srcClass, newKey, false))));

            verify(twinClassService, never()).saveSafe(any(Collection.class));
        }

        @Test
        @Disabled("TODO: implement existsById/existsByDomainIdAndKey check before saveSafe")
        void throwsForEachConflictingKeyInBatch() throws ServiceException {
            TwinClassEntity src2 = new TwinClassEntity().setId(UUID.randomUUID()).setDomainId(domainId);
            String conflictKey = "conflict_key";

            stubSaveSafeAndCapture();

            assertThrows(ServiceException.class,
                    () -> twinClassDuplicateService.duplicate(List.of(
                            duplicateOf(srcClass, "key_ok", false),
                            duplicateOf(src2, conflictKey, false)
                    )));

            verify(twinClassService, never()).saveSafe(any(Collection.class));
        }
    }
}

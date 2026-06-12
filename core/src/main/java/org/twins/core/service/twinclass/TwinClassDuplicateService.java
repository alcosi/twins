package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KeyUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.twinclass.TwinClassDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinStatusService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassDuplicateService extends EntityDuplicateService<TwinClassDuplicate, TwinClassEntity> {

    @Lazy
    private final TwinClassService twinClassService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinStatusService twinStatusService;
    private final I18nService i18nService;
    @Lazy
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinClassEntity> entityService() {
        return twinClassService;
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void prepareDuplicates(Collection<TwinClassDuplicate> duplicates) throws ServiceException {
        var apiUser = authService.getApiUser();
        for (var duplicate : duplicates) {
            duplicate.setNewTwinClassId(UUID.nameUUIDFromBytes((duplicate.getNewKey() + apiUser.getDomainId()).getBytes()));
        }
    }

    @Override
    protected TwinClassEntity createNewEntity(TwinClassDuplicate duplicate, TwinClassEntity original) throws ServiceException {
        log.info("{} will be duplicated with new key[{}]", original.logShort(), duplicate.getNewKey());
        return new TwinClassEntity()
                .setKey(KeyUtils.upperCaseNullFriendly(duplicate.getNewKey(), ErrorCodeTwins.TWIN_CLASS_KEY_INCORRECT))
                .setCreatedByUserId(authService.getApiUser().getUser().getId())
                .setPermissionSchemaSpace(original.getPermissionSchemaSpace())
                .setTwinflowSchemaSpace(original.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(original.getTwinClassSchemaSpace())
                .setAliasSpace(original.getAliasSpace())
                .setAssigneeRequired(original.getAssigneeRequired())
                .setAbstractt(original.getAbstractt())
                .setUniqueName(original.getUniqueName())
                .setExtendsTwinClassId(original.getExtendsTwinClassId())
                .setHeadTwinClassId(original.getHeadTwinClassId())
                .setIconDarkResourceId(original.getIconDarkResourceId())
                .setIconDarkResource(original.getIconDarkResource())
                .setIconLightResourceId(original.getIconLightResourceId())
                .setIconLightResource(original.getIconLightResource())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(original.getDomainId())
                .setOwnerType(original.getOwnerType())
                .setViewPermissionId(original.getViewPermissionId())
                .setCreatePermissionId(original.getCreatePermissionId())
                .setSegment(original.getSegment())
                .setHasSegment(false)
                .setMarkerDataListId(original.getMarkerDataListId())
                .setTagDataListId(original.getTagDataListId())
                .setHeadHunterFeaturerId(original.getHeadHunterFeaturerId())
                .setHeadHunterParams(original.getHeadHunterParams())
                .setHasDynamicMarkers(false)
                .setPageFaceId(original.getPageFaceId())
                .setBreadCrumbsFaceId(original.getBreadCrumbsFaceId())
                .setGeneralAttachmentRestrictionId(original.getGeneralAttachmentRestrictionId())
                .setCommentAttachmentRestrictionId(original.getCommentAttachmentRestrictionId())
                .setExternalId(original.getExternalId())
                .setExternalProperties(original.getExternalProperties())
                .setExternalJson(original.getExternalJson())
                .setHeadHierarchyCounterDirectChildren(0)
                .setExtendsHierarchyCounterDirectChildren(0)
                .setTwinCounter(0);
    }

    @Override
    protected void duplicateI18nFields(TwinClassEntity src, TwinClassEntity dst) throws ServiceException {
        I18nEntity i18nDuplicate;
        if (src.getNameI18NId() != null) {
            i18nDuplicate = i18nService.duplicateI18n(src.getNameI18NId());
            dst.setNameI18NId(i18nDuplicate.getId());
        }
        if (src.getDescriptionI18NId() != null) {
            i18nDuplicate = i18nService.duplicateI18n(src.getDescriptionI18NId());
            dst.setDescriptionI18NId(i18nDuplicate.getId());
        }
    }

    @Override
    protected void afterSave(Collection<TwinClassDuplicate> duplicates, Collection<TwinClassEntity> saved) throws ServiceException {
        for (var savedClass : saved) {
            twinClassService.refreshExtendsHierarchyTree(savedClass);
            twinClassService.refreshHeadHierarchyTree(savedClass);
        }
        var needLoadFields = new ArrayList<TwinClassEntity>();
        var needLoadStatuses = new ArrayList<TwinClassEntity>();
        for (var duplicate : duplicates) {
            if (duplicate.isDuplicateFields()) {
                needLoadFields.add(duplicate.getOriginalEntity());
            }
            if (duplicate.isDuplicateStatuses()) {
                needLoadStatuses.add(duplicate.getOriginalEntity());
            }
        }
        if (CollectionUtils.isNotEmpty(needLoadFields)) {
            twinClassFieldService.loadTwinClassFields(needLoadFields);
        }
        if (CollectionUtils.isNotEmpty(needLoadStatuses)) {
            twinStatusService.loadStatusesForTwinClasses(needLoadStatuses);
        }
        for (var duplicate : duplicates) {
            if (duplicate.isDuplicateFields()) {
                twinClassFieldService.duplicateFieldsForClass(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicateStatuses()) {
                twinStatusService.duplicateStatusesForClass(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
        }
    }
}

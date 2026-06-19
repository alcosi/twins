package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KeyUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.twinclass.TwinClassDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinStatusDuplicateService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassDuplicateService extends EntityDuplicateService<TwinClassDuplicate, TwinClassEntity, Void> {
    @Lazy
    private final TwinClassService twinClassService;
    @Lazy
    private final TwinClassFieldDuplicateService twinClassFieldDuplicateService;
    @Lazy
    private final TwinStatusDuplicateService twinStatusDuplicateService;
    private final I18nService i18nService;
    @Lazy
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinClassEntity> entityService() {
        return twinClassService;
    }

    @Override
    protected EntitySecureFindServiceImpl<Void> entityParentService() {
        return null; // top-level entity
    }

    @Override
    protected Class<TwinClassEntity> getEntityClass() {
        return TwinClassEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(); // top-level
    }

    @Override
    protected TwinClassDuplicate createNewDuplicate() {
        return new TwinClassDuplicate();
    }

    @Override
    protected void loadFor(Collection<Void> parents) {
        // top-level entity — no parent, nothing to load
    }

    @Override
    protected Kit<TwinClassEntity, UUID> extractorChildren(Void parent) {
        return null; // top-level entity — never invoked
    }

    @Override
    protected UUID extractParentId(Void parent) {
        return null; // top-level entity — never invoked
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE;
    }

    @Override
    protected TwinClassEntity createNewEntity(TwinClassDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        TwinClassEntity original = duplicate.getOriginalEntity();
        log.info("{} will be duplicated with new key[{}]", original.logShort(), duplicate.getNewKey());
        return new TwinClassEntity()
                .setId(duplicate.getNewEntityId())
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
    protected void collectDuplicatesTree(Collection<TwinClassDuplicate> duplicates, EntityDuplicateCollector ctx) throws ServiceException {
        Map<TwinClassEntity, TwinClassEntity> copyStatusesFor = null;
        Map<TwinClassEntity, TwinClassEntity> copyFieldsFor = null;
        for (var duplicate : duplicates) {
            if (duplicate.isDuplicateFields()) {
                if (copyFieldsFor == null) copyFieldsFor = new HashMap<>();
                copyFieldsFor.put(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
            if (duplicate.isDuplicateStatuses()) {
                if (copyStatusesFor == null) copyStatusesFor = new HashMap<>();
                copyStatusesFor.put(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
        }
        if (copyFieldsFor != null) {
            twinClassFieldDuplicateService.collectViaParentMap(ctx, copyFieldsFor);
        }
        if (copyStatusesFor != null) {
            twinStatusDuplicateService.collectViaParentMap(ctx, copyStatusesFor);
        }
    }

    @Override
    protected void afterCommit(Collection<TwinClassDuplicate> duplicates, Collection<TwinClassEntity> saved, EntityDuplicateCollector ctx) throws ServiceException {
        for (var savedClass : saved) {
            twinClassService.refreshExtendsHierarchyTree(savedClass);
            twinClassService.refreshHeadHierarchyTree(savedClass);
        }
    }

    @Override
    protected void setNewParentEntity(TwinClassEntity newEntity, Void parentEntity) {
        // no parent
    }
}

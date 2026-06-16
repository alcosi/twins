package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KeyUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.i18n.I18nService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldDuplicateService extends EntityDuplicateService<TwinClassFieldDuplicate, TwinClassFieldEntity, TwinClassEntity> {

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final I18nService i18nService;

    @Override
    protected EntitySecureFindServiceImpl<TwinClassFieldEntity> entityService() {
        return twinClassFieldService;
    }

    @Override
    protected TwinClassFieldDuplicate createNewDuplicate() {
        return new TwinClassFieldDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinClassEntity> parents) {
        twinClassFieldService.loadTwinClassFields(parents);
    }

    @Override
    protected Kit<TwinClassFieldEntity, UUID> extractorChildren(TwinClassEntity parent) {
        return parent.getTwinClassFieldKit();
    }

    @Override
    protected UUID extractParentId(TwinClassEntity parent) {
        return parent.getId();
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT;
    }

    @Override
    protected TwinClassFieldEntity createNewEntity(TwinClassFieldDuplicate duplicate) throws ServiceException {
        TwinClassFieldEntity original = duplicate.getOriginalEntity();
        return new TwinClassFieldEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT))
                .setTwinClassId(original.getId())
                .setFieldTyperFeaturerId(original.getFieldTyperFeaturerId())
                .setFieldTyperParams(original.getFieldTyperParams())
                .setTwinSorterFeaturerId(original.getTwinSorterFeaturerId())
                .setTwinSorterParams(original.getTwinSorterParams())
                .setFieldInitializerFeaturerId(original.getFieldInitializerFeaturerId())
                .setFieldInitializerParams(original.getFieldInitializerParams())
                .setViewPermissionId(original.getViewPermissionId())
                .setEditPermissionId(original.getEditPermissionId())
                .setRequired(original.getRequired())
                .setExternalId(original.getExternalId())
                .setExternalProperties(original.getExternalProperties())
                .setSystem(original.getSystem())
                .setInheritable(original.getInheritable())
                .setDependentField(false)
                .setHasDependentFields(false)
                .setOrder(original.getOrder())
                .setProjectionField(false)
                .setHasProjectedFields(false);
    }

    @Override
    protected void duplicateI18nFields(TwinClassFieldEntity src, TwinClassFieldEntity dst) {
        if (src.getNameI18nId() != null) {
            dst.setNameI18nId(i18nService.duplicateI18n(src.getNameI18nId()).getId());
        }
        if (src.getDescriptionI18nId() != null) {
            dst.setDescriptionI18nId(i18nService.duplicateI18n(src.getDescriptionI18nId()).getId());
        }
        if (src.getFeValidationErrorI18nId() != null) {
            dst.setFeValidationErrorI18nId(i18nService.duplicateI18n(src.getFeValidationErrorI18nId()).getId());
        }
        if (src.getBeValidationErrorI18nId() != null) {
            dst.setBeValidationErrorI18nId(i18nService.duplicateI18n(src.getBeValidationErrorI18nId()).getId());
        }
    }

    @Override
    protected void setNewParentEntityId(TwinClassFieldEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinClassId(duplicateParentEntityId);
    }
}

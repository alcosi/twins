package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.i18n.I18nService;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldDuplicateService extends EntityDuplicateService<TwinClassFieldDuplicate, TwinClassFieldEntity> {

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinClassService twinClassService;
    private final I18nService i18nService;

    @Override
    protected EntitySecureFindServiceImpl<TwinClassFieldEntity> entityService() {
        return twinClassFieldService;
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT;
    }

    @Override
    protected void prepareDuplicates(Collection<TwinClassFieldDuplicate> duplicates) throws ServiceException {
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinClassId() == null) {
                TwinClassFieldEntity original = duplicate.getOriginalEntity();
                duplicate
                        .setNewTwinClassId(original.getTwinClassId())
                        .setNewTwinClass(original.getTwinClass());
            }
        }
        twinClassService.load(duplicates,
                TwinClassFieldDuplicate::getNewTwinClassId,
                TwinClassFieldDuplicate::getNewTwinClass,
                TwinClassFieldDuplicate::setNewTwinClass);
    }

    @Override
    protected TwinClassFieldEntity createNewEntity(TwinClassFieldDuplicate duplicate) throws ServiceException {
        TwinClassFieldEntity original = duplicate.getOriginalEntity();
        TwinClassEntity targetClass = duplicate.getNewTwinClass();
        log.info("{} will be duplicated for {}", original.logShort(), targetClass.logNormal());
        return new TwinClassFieldEntity()
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT))
                .setTwinClassId(targetClass.getId())
                .setTwinClass(targetClass)
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

    @Transactional
    public void duplicateFieldsForClass(TwinClassEntity fromTwinClass, TwinClassEntity toTwinClass) throws ServiceException {
        twinClassFieldService.loadTwinClassFields(fromTwinClass);
        if (KitUtils.isEmpty(fromTwinClass.getTwinClassFieldKit())) {
            return;
        }
        var duplicates = new ArrayList<TwinClassFieldDuplicate>();
        for (TwinClassFieldEntity originalField : fromTwinClass.getTwinClassFieldKit().getCollection()) {
            if (!originalField.getTwinClassId().equals(fromTwinClass.getId()))
                continue; //skipping inherited fields
            var dummy = new TwinClassFieldDuplicate();
            dummy.setOriginalEntity(originalField);
            dummy.setNewKey(originalField.getKey());
            dummy.setNewTwinClass(toTwinClass);
            dummy.setNewTwinClassId(toTwinClass.getId());
            duplicates.add(dummy);
        }
        duplicate(duplicates);
    }
}

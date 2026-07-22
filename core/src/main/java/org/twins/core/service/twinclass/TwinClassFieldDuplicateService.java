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
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.twinclass.TwinClassFieldDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldDuplicateService extends EntityDuplicateService<TwinClassFieldDuplicate, TwinClassFieldEntity, TwinClassEntity> {

    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    protected EntitySecureFindServiceImpl<TwinClassFieldEntity> entityService() {
        return twinClassFieldService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinClassEntity> entityParentService() {
        return twinClassService;
    }

    @Override
    protected Class<TwinClassFieldEntity> getEntityClass() {
        return TwinClassFieldEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinClassEntity.class);
    }

    @Override
    protected TwinClassFieldDuplicate createNewDuplicate() {
        return new TwinClassFieldDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinClassEntity> parents) {
        twinClassFieldService.loadTwinClassFields(parents);
    }

    /**
     * Enables "duplicate in place": a class field duplicated without an explicit target class
     * ({@code newTwinClassId == null}) defaults to its own class. Restores the pre-TWINS-798
     * {@code TwinClassFieldService.duplicateFields} behavior.
     */
    @Override
    protected UUID extractOriginalParentId(TwinClassFieldEntity original) {
        return original.getTwinClassId();
    }

    /**
     * Cascaded class fields (from a class duplicate with {@code duplicateFields=true}) reuse the
     * original key. Safe because {@code twin_class_field.key} is unique per twin_class and the clone
     * lands in a different class.
     */
    @Override
    protected String extractOriginalKey(TwinClassFieldEntity original) {
        return original.getKey();
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
    protected TwinClassFieldEntity createNewEntity(TwinClassFieldDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        TwinClassFieldEntity original = duplicate.getOriginalEntity();
        return new TwinClassFieldEntity()
                .setId(null)
                .setKey(KeyUtils.lowerCaseNullSafe(resolveKey(duplicate), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT))
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
    protected List<I18nFieldDuplicate<TwinClassFieldEntity>> i18nFields() {
        return List.of(
                I18nFieldDuplicate.of(TwinClassFieldEntity::getNameI18nId,            TwinClassFieldEntity::setNameI18nId),
                I18nFieldDuplicate.of(TwinClassFieldEntity::getDescriptionI18nId,     TwinClassFieldEntity::setDescriptionI18nId),
                I18nFieldDuplicate.of(TwinClassFieldEntity::getFeValidationErrorI18nId, TwinClassFieldEntity::setFeValidationErrorI18nId),
                I18nFieldDuplicate.of(TwinClassFieldEntity::getBeValidationErrorI18nId, TwinClassFieldEntity::setBeValidationErrorI18nId)
        );
    }

    @Override
    protected void setNewParentEntity(TwinClassFieldEntity newEntity, TwinClassEntity parentEntity) {
        newEntity
                .setTwinClassId(parentEntity.getId())
                .setTwinClass(parentEntity);
    }
}

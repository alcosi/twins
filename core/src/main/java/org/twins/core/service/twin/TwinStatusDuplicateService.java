package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.twinstatus.TwinStatusDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusDuplicateService extends EntityDuplicateService<TwinStatusDuplicate, TwinStatusEntity, TwinClassEntity> {

    @Lazy
    private final TwinStatusService twinStatusService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    protected EntitySecureFindServiceImpl<TwinStatusEntity> entityService() {
        return twinStatusService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinClassEntity> entityParentService() {
        return twinClassService;
    }

    @Override
    protected Class<TwinStatusEntity> getEntityClass() {
        return TwinStatusEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinClassEntity.class);
    }

    @Override
    protected TwinStatusDuplicate createNewDuplicate() {
        return new TwinStatusDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinClassEntity> parents) {
        twinStatusService.loadStatuses(parents);
    }

    @Override
    protected Kit<TwinStatusEntity, UUID> extractorChildren(TwinClassEntity parent) {
        return parent.getTwinStatusKit();
    }

    @Override
    protected UUID extractParentId(TwinClassEntity parent) {
        return parent.getId();
    }

    @Override
    protected org.cambium.common.exception.ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT;
    }

    @Override
    protected TwinStatusEntity createNewEntity(TwinStatusDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        TwinStatusEntity original = duplicate.getOriginalEntity();
        return new TwinStatusEntity()
                .setId(UuidUtils.generate())
                .setKey(KeyUtils.lowerCaseNullSafe(duplicate.getNewKey(), ErrorCodeTwins.TWIN_STATUS_KEY_INCORRECT))
                .setInheritable(original.getInheritable())
                .setBackgroundColor(original.getBackgroundColor())
                .setFontColor(original.getFontColor())
                .setType(original.getType());
    }

    @Override
    protected List<I18nFieldDuplicate<TwinStatusEntity>> i18nFields() {
        return List.of(
                I18nFieldDuplicate.of(TwinStatusEntity::getNameI18nId,        TwinStatusEntity::setNameI18nId),
                I18nFieldDuplicate.of(TwinStatusEntity::getDescriptionI18nId, TwinStatusEntity::setDescriptionI18nId)
        );
    }

    @Override
    protected void setNewParentEntity(TwinStatusEntity newEntity, TwinClassEntity parentEntity) {
        newEntity
                .setTwinClassId(parentEntity.getId())
                .setTwinClass(parentEntity);
    }
}

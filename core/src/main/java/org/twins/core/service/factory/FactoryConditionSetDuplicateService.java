package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryConditionSetDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryConditionSetDuplicateService extends EntityDuplicateService<FactoryConditionSetDuplicate, TwinFactoryConditionSetEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;
    @Lazy
    private final FactoryConditionDuplicateService factoryConditionDuplicateService;
    @Lazy
    private final FactoryService factoryService;
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryConditionSetEntity> entityService() {
        return factoryConditionSetService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityParentService() {
        return factoryService;
    }

    @Override
    protected Class<TwinFactoryConditionSetEntity> getEntityClass() {
        return TwinFactoryConditionSetEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryEntity.class);
    }

    @Override
    protected FactoryConditionSetDuplicate createNewDuplicate() {
        return new FactoryConditionSetDuplicate()
                .setDuplicateConditions(true);
    }

    @Override
    protected void loadFor(Collection<TwinFactoryEntity> parents) {
        factoryConditionSetService.loadFactoryConditionSets(parents);
    }

    @Override
    protected Kit<TwinFactoryConditionSetEntity, UUID> extractorChildren(TwinFactoryEntity parent) {
        return parent.getTwinFactoryConditionSetKit();
    }

    @Override
    protected UUID extractParentId(TwinFactoryEntity parent) {
        return parent.getId();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryConditionSetDuplicate> duplicates) throws ServiceException {
        // condition sets have no key concept
    }

    @Override
    protected TwinFactoryConditionSetEntity createNewEntity(FactoryConditionSetDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryConditionSetEntity()
                .setId(duplicate.getNewEntityId())
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setDomainId(src.getDomainId())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setCachable(src.getCachable() == null ? Boolean.TRUE : src.getCachable())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(authService.getApiUser().getUserId());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryConditionSetEntity src, TwinFactoryConditionSetEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntity(TwinFactoryConditionSetEntity newEntity, TwinFactoryEntity parentEntity) {
        newEntity
                .setTwinFactoryId(parentEntity.getId())
                .setTwinFactory(parentEntity);
    }

    @Override
    protected void collectDuplicatesTree(Collection<FactoryConditionSetDuplicate> duplicates, EntityDuplicateCollector ctx) throws ServiceException {
        Map<TwinFactoryConditionSetEntity, TwinFactoryConditionSetEntity> conditionsMap = null;
        for (var duplicate : duplicates) {
            if (duplicate.isDuplicateConditions()) {
                if (conditionsMap == null) conditionsMap = new HashMap<>();
                conditionsMap.put(duplicate.getOriginalEntity(), duplicate.getNewEntity());
            }
        }
        if (conditionsMap != null) {
            factoryConditionDuplicateService.collectViaParentMap(ctx, conditionsMap);
        }
    }
}

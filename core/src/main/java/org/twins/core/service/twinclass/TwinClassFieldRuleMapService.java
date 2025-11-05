package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleMapEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleMapRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleMapService extends EntitySecureFindServiceImpl<TwinClassFieldRuleMapEntity> {
    private final TwinClassFieldRuleMapRepository twinClassFieldRuleMapRepository;
    private final TwinClassService twinClassService;

    @Lazy
    private final TwinClassFieldRuleService twinClassFieldRuleService;



    @Override
    public CrudRepository<TwinClassFieldRuleMapEntity, UUID> entityRepository() {
        return twinClassFieldRuleMapRepository;
    }

    @Override
    public Function<TwinClassFieldRuleMapEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldRuleMapEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldRuleMapEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldRuleMapEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassFieldRuleId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassFieldRuleId");
        if (entity.getTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassFieldId");

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldRuleMapEntity> createRuleMaps(List<TwinClassFieldRuleMapEntity> twinClassFieldRuleMapEntities) throws ServiceException {
        entitySmartService.saveAllAndLog(twinClassFieldRuleMapEntities, twinClassFieldRuleMapRepository);
        return twinClassFieldRuleMapEntities;
    }

    public void loadRules(TwinClassFieldEntity fieldEntity) throws ServiceException {
        loadRules(Collections.singleton(fieldEntity));
    }

    public void loadRules(Collection<TwinClassFieldEntity> fieldEntities) throws ServiceException {
        Kit<TwinClassFieldEntity, UUID> needLoad = new Kit<>(TwinClassFieldEntity::getId);
        fieldEntities.stream()
                .filter(field -> field.getRuleKit() == null)
                .forEach(needLoad::add);

        if (needLoad.isEmpty()) return;

        List<TwinClassFieldRuleMapEntity> ruleMaps = twinClassFieldRuleMapRepository.findByTwinClassFieldIdIn(needLoad.getIdSet());

        if (ruleMaps.isEmpty()) {
            needLoad.forEach(field -> field.setRuleKit(Kit.EMPTY));
            return;
        }

        Kit<TwinClassFieldRuleEntity, UUID> rulesKit = twinClassFieldRuleService.findEntitiesSafe(
                ruleMaps.stream()
                        .map(TwinClassFieldRuleMapEntity::getTwinClassFieldRuleId)
                        .collect(Collectors.toSet())
        );

        Map<UUID, UUID> fieldIdByRuleId = ruleMaps.stream()
                .collect(Collectors.toMap(
                        TwinClassFieldRuleMapEntity::getTwinClassFieldRuleId,
                        TwinClassFieldRuleMapEntity::getTwinClassFieldId
                ));

        KitGrouped<TwinClassFieldRuleEntity, UUID, UUID> rulesGrouped = new KitGrouped<>(
                rulesKit,
                TwinClassFieldRuleEntity::getId,
                rule -> fieldIdByRuleId.get(rule.getId())
        );

        for (TwinClassFieldEntity fieldEntity : needLoad) {
            if (rulesGrouped.containsGroupedKey(fieldEntity.getId()))
                fieldEntity.setRuleKit(new Kit<>(rulesGrouped.getGrouped(fieldEntity.getId()), TwinClassFieldRuleEntity::getId));
            else
                fieldEntity.setRuleKit(Kit.EMPTY);
        }
    }

    public void deleteRuleMaps(UUID twinClassId) throws ServiceException {
        if (twinClassId == null)
            return;
        twinClassService.findEntitySafe(twinClassId);
        twinClassFieldRuleMapRepository.deleteByTwinClassId(twinClassId);
    }

    public List<TwinClassFieldRuleMapEntity> findByTwinClassFieldRuleIdIn(Collection<UUID> fieldIds) {
        return twinClassFieldRuleMapRepository.findByTwinClassFieldRuleIdIn(fieldIds);
    }
}

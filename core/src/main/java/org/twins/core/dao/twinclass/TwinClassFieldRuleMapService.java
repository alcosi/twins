package org.twins.core.dao.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.service.twinclass.TwinClassFieldRuleService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TwinClassFieldRuleMapService extends EntitySecureFindServiceImpl<TwinClassFieldRuleMapEntity> {
    private final TwinClassFieldRuleMapRepository twinClassFieldRuleMapRepository;
    private final TwinClassFieldRuleService twinClassFieldRuleService;
    private final TwinClassService twinClassService;


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

        Kit<TwinClassFieldRuleMapEntity, UUID> ruleMapsKit = findEntitiesSafe(needLoad.getIdSet());

        if (ruleMapsKit.isEmpty()) {
            needLoad.forEach(field -> field.setRuleKit(Kit.EMPTY));
            return;
        }

        Kit<TwinClassFieldRuleEntity, UUID> rulesKit = twinClassFieldRuleService.findEntitiesSafe(
                ruleMapsKit.stream()
                        .map(TwinClassFieldRuleMapEntity::getTwinClassFieldRuleId)
                        .collect(Collectors.toSet())
        );

        Map<UUID, List<UUID>> ruleIdsByFieldId = ruleMapsKit.getList().stream()
                .collect(Collectors.groupingBy(
                        TwinClassFieldRuleMapEntity::getTwinClassFieldId,
                        Collectors.mapping(TwinClassFieldRuleMapEntity::getTwinClassFieldRuleId, Collectors.toList())
                ));

        needLoad.forEach(field -> {
            List<UUID> ruleIds = ruleIdsByFieldId.get(field.getId());
            if (ruleIds != null && !ruleIds.isEmpty()) {
                Kit<TwinClassFieldRuleEntity, UUID> fieldRuleKit = new Kit<>(
                        ruleIds.stream()
                                .map(rulesKit::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()),
                        TwinClassFieldRuleEntity::getId
                );
                field.setRuleKit(fieldRuleKit.isNotEmpty() ? fieldRuleKit : Kit.EMPTY);
            } else {
                field.setRuleKit(Kit.EMPTY);
            }
        });
    }

    public void deleteRuleMaps(UUID twinClassId) throws ServiceException {
        if (twinClassId == null)
            return;
        twinClassService.findEntitySafe(twinClassId);
        twinClassFieldRuleMapRepository.deleteByTwinClassId(twinClassId);
    }
}

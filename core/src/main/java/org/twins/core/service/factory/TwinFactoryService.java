package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.conditioner.Conditioner;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinFactoryService extends EntitySecureFindServiceImpl<TwinFactoryEntity> {
    final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    final TwinService twinService;
    final TwinClassService twinClassService;
    final TwinFactoryConditionRepository twinFactoryConditionRepository;
    final TwinFactoryRepository twinFactoryRepository;
    @Lazy
    final FeaturerService featurerService;

    @Override
    public CrudRepository<TwinFactoryEntity, UUID> entityRepository() {
        return twinFactoryRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinOperation> runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        TwinFactoryEntity factoryEntity = findEntitySafe(factoryId);
        return runFactory(factoryEntity, factoryContext);
    }

    public List<TwinOperation> runFactory(TwinFactoryEntity factoryEntity, FactoryContext factoryContext) throws ServiceException {
        log.info("Running " + factoryEntity.logNormal());
        List<TwinFactoryMultiplierEntity> factoryMultiplierEntityList = twinFactoryMultiplierRepository.findByTwinFactoryId(factoryEntity.getId()); //few multipliers can be attached to one factory, because one can be used to create on grouped twin, other for create isolated new twin and so on
        Map<UUID, List<TwinEntity>> factoryInputTwins = groupItemsByClass(factoryContext);
        for (TwinFactoryMultiplierEntity factoryMultiplierEntity : factoryMultiplierEntityList) {
            List<TwinEntity> multiplierInput = factoryInputTwins.get(factoryMultiplierEntity.getInputTwinClassId());
            if (CollectionUtils.isEmpty(multiplierInput)) {
                log.info(factoryMultiplierEntity.logNormal() + " empty input");
                continue;
            }
            Multiplier multiplier = featurerService.getFeaturer(factoryMultiplierEntity.getMultiplierFeaturer(), Multiplier.class);
            List<FactoryItem> multiplierOutput = multiplier.multiply(factoryMultiplierEntity, multiplierInput, factoryContext);
            factoryContext.getFactoryItemList().addAll(multiplierOutput);
        }
        List<TwinFactoryPipelineEntity> factoryPipelineEntityList = twinFactoryPipelineRepository.findByTwinFactoryIdAndActiveTrue(factoryEntity.getId());
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityList) {
            log.info("Checking " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            List<FactoryItem> pipelineInputList = new ArrayList<>();
            for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
                if (twinClassService.isInstanceOf(factoryItem.getOutputTwin().getTwinEntity().getTwinClass(), factoryPipelineEntity.getInputTwinClassId())) {
                    if (checkCondition(factoryPipelineEntity.getTwinFactoryConditionSetId(), factoryPipelineEntity.isTwinFactoryConditionInvert(), factoryItem))
                        pipelineInputList.add(factoryItem);
                }
            }
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info("Skipping " + factoryPipelineEntity.logShort() + " because of empty input");
                continue;
            }
            log.info("Running " + factoryPipelineEntity.logNormal() + " **" + factoryPipelineEntity.getDescription() + "** ");
            List<TwinFactoryPipelineStepEntity> pipelineStepEntityList = twinFactoryPipelineStepRepository.findByTwinFactoryPipelineIdAndActiveTrueOrderByOrder(factoryPipelineEntity.getId());
            for (FactoryItem pipelineInput : pipelineInputList) {
                pipelineInput.setFactoryContext(factoryContext); // setting global factory context to be accessible from fillers
                if (pipelineInput.getOutputTwin().getTwinEntity().getId() == null)
                    pipelineInput.getOutputTwin().getTwinEntity().setId(UUID.randomUUID()); //generating id for using in fillers (if some field must be created)
                String logMsg;
                for (int step = 0; step < pipelineStepEntityList.size(); step++) {
                    TwinFactoryPipelineStepEntity pipelineStepEntity = pipelineStepEntityList.get(step);
                    if (!checkCondition(pipelineStepEntity.getTwinFactoryConditionSetId(), pipelineStepEntity.isTwinFactoryConditionInvert(), pipelineInput)) {
                        log.info("Step " + (step + 1) + "/" + pipelineStepEntityList.size() + " **" + pipelineStepEntity.getComment() + "** was skipped)");
                        continue;
                    }
                    Filler filler = featurerService.getFeaturer(pipelineStepEntity.getFillerFeaturer(), Filler.class);
                    logMsg = "Step " + (step + 1) + "/" + pipelineStepEntityList.size() + " **" + pipelineStepEntity.getComment() + "**)";
                    filler.fill(pipelineStepEntity.getFillerParams(), pipelineInput, factoryPipelineEntity.getTemplateTwin(), logMsg);
                }
                if (factoryPipelineEntity.getOutputTwinStatusId() != null) {
                    log.info("Pipeline output twin status[" + factoryPipelineEntity.getOutputTwinStatusId() + "]");
                    pipelineInput.getOutputTwin().getTwinEntity()
                            .setTwinStatus(factoryPipelineEntity.getOutputTwinStatus())
                            .setTwinStatusId(factoryPipelineEntity.getOutputTwinStatusId());
                }

            }
            if (factoryPipelineEntity.getNextTwinFactoryId() != null) {
                log.info(factoryPipelineEntity.logShort() + " has nextFactoryId configured");
                runFactory(factoryPipelineEntity.getNextTwinFactoryId(), factoryContext); //todo endless recursion risk
            }
        }
        return factoryContext.getFactoryItemList().stream().map(FactoryItem::getOutputTwin).toList();
    }

    private Map<UUID, List<TwinEntity>> groupItemsByClass(FactoryContext factoryContext) {
        Map<UUID, List<TwinEntity>> factoryInputTwins = new HashMap<>();
        for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
            TwinOperation twinOperation = factoryItem.getOutputTwin();
            List<TwinEntity> twinsGroupedByClass = factoryInputTwins.computeIfAbsent(factoryItem.getOutputTwin().getTwinEntity().getTwinClassId(), k -> new ArrayList<>());
            if (twinOperation instanceof TwinUpdate twinUpdate)
                twinsGroupedByClass.add(twinUpdate.getDbTwinEntity());
            else
                twinsGroupedByClass.add(twinOperation.getTwinEntity());
        }
        return factoryInputTwins;
    }
    private boolean checkCondition(UUID conditionSetId, boolean twinFactoryConditionInvert, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        boolean ret = checkCondition(conditionSetId, factoryItem);
        return twinFactoryConditionInvert ? !ret : ret;
    }

    //todo result can be cached in session cache
//    @Cacheable(value = "TwinFactoryService.checkCondition", key = "{#conditionSetId, #factoryItem.hashCode() }", cacheManager = "cacheManagerRequestScope")
    public boolean checkCondition(UUID conditionSetId, FactoryItem factoryItem) throws ServiceException {
        if (conditionSetId == null)
            return true;
        List<TwinFactoryConditionEntity> conditionEntityList = twinFactoryConditionRepository.findByTwinFactoryConditionSetIdAndActiveTrue(conditionSetId);
        boolean ret = true;
        for (TwinFactoryConditionEntity conditionEntity : conditionEntityList) {
            Conditioner conditioner = featurerService.getFeaturer(conditionEntity.getConditionerFeaturer(), Conditioner.class);
            ret = ret && conditioner.check(conditionEntity, factoryItem);
            if (!ret) // no need to check other conditions if one of it is already false
                break;
        }
        return ret;
    }
}

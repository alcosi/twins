package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinOperation;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.factory.filler.Filler;
import org.twins.core.featurer.factory.multiplier.Multiplier;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinFactoryService {
    final TwinFactoryMultiplierRepository twinFactoryMultiplierRepository;
    final TwinFactoryPipelineRepository twinFactoryPipelineRepository;
    final TwinFactoryPipelineStepRepository twinFactoryPipelineStepRepository;
    final TwinService twinService;
    final TwinClassService twinClassService;
    @Lazy
    final FeaturerService featurerService;

    public List<TwinOperation> runFactory(UUID factoryId, FactoryContext factoryContext) throws ServiceException {
        log.info("Running factory[" + factoryId + "]");
        List<TwinFactoryMultiplierEntity> factoryMultiplierEntityList = twinFactoryMultiplierRepository.findByTwinFactoryId(factoryId); //few multipliers can be attached to one factory, because one can be used to create on grouped twin, other for create isolated new twin and so on
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
        List<TwinFactoryPipelineEntity> factoryPipelineEntityList = twinFactoryPipelineRepository.findByTwinFactoryId(factoryId);
        for (TwinFactoryPipelineEntity factoryPipelineEntity : factoryPipelineEntityList) {
            log.info("Running " + factoryPipelineEntity.logNormal());
            List<FactoryItem> pipelineInputList = new ArrayList<>();
            for (FactoryItem factoryItem : factoryContext.getFactoryItemList()) {
                if (twinClassService.isInstanceOf(factoryItem.getOutputTwin().getTwinEntity().getTwinClass(), factoryPipelineEntity.getInputTwinClassId()))
                    pipelineInputList.add(factoryItem);
            }
            if (CollectionUtils.isEmpty(pipelineInputList)) {
                log.info(factoryPipelineEntity.logShort() + " empty input");
                continue;
            }
            List<TwinFactoryPipelineStepEntity> pipelineStepEntityList = twinFactoryPipelineStepRepository.findByTwinFactoryPipelineId(factoryPipelineEntity.getId());
            for (FactoryItem pipelineInput : pipelineInputList) {
                pipelineInput.setFactoryContext(factoryContext); // setting global factory context to be accessible from fillers
                if (pipelineInput.getOutputTwin().getTwinEntity().getId() == null)
                    pipelineInput.getOutputTwin().getTwinEntity().setId(UUID.randomUUID()); //generating id for using in fillers (if some field must be created)
                String logMsg;
                for (TwinFactoryPipelineStepEntity pipelineStepEntity : pipelineStepEntityList) {
                    Filler filler = featurerService.getFeaturer(pipelineStepEntity.getFillerFeaturer(), Filler.class);
                    logMsg = "Step " + pipelineStepEntity.getOrder() + "/" + pipelineStepEntityList.size() + " **" + pipelineStepEntity.getComment() + "**)";
                    filler.fill(pipelineStepEntity.getFillerParams(), pipelineInput, factoryPipelineEntity.getTemplateTwin(), logMsg);
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
}

package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinEraserService {
    private final TwinRepository twinRepository;
    private final EntitySmartService entitySmartService;
    private final TwinEraserTransactionScopeRepository twinEraserTransactionScopeRepository;
    @Lazy
    private final TwinLinkService twinLinkService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final TwinflowService twinflowService;

    public void deleteTwinForever(UUID twinId) throws ServiceException {
        Set<UUID> deletionSet = new HashSet<>();
        deletionSet.add(twinId);
        boolean deeperLinksFound;
        final List<LinkStrength> strengthIds = LinkStrength.getForCascadeDeletion();
        do {
            deeperLinksFound = false;
            List<TwinLinkEntity> links = twinLinkService.findTwinBackwardLinksAndLinkStrengthIds(deletionSet, strengthIds);
            for (TwinLinkEntity link : links)
                if (deletionSet.add(link.getSrcTwinId())) deeperLinksFound = true;
        } while (deeperLinksFound);
        deleteTwinsForever(deletionSet);
    }

    public void deleteTwinsForever(Collection<UUID> twinIds) {
        entitySmartService.deleteAllAndLog(twinIds, twinRepository);// all linked data will be deleted by fk cascading
    }

    public void deleteTwin(UUID twinId) throws ServiceException {
        deleteTwin(twinService.findEntitySafe(twinId));
    }

    public void deleteTwin(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinClass().getDeleteTwinFactoryId() != null) {

        }
        Set<UUID> deletionSet = new HashSet<>();
        deletionSet.add(twinEntity);
        boolean deeperLinksFound;
        final List<LinkStrength> strengthIds = LinkStrength.getForCascadeDeletion();
        do {
            deeperLinksFound = false;
            List<TwinLinkEntity> links = twinLinkService.findTwinBackwardLinksAndLinkStrengthIds(deletionSet, strengthIds);
            for (TwinLinkEntity link : links)
                if (deletionSet.add(link.getSrcTwinId())) deeperLinksFound = true;
        } while (deeperLinksFound);
        deleteTwins(deletionSet);
    }

    public void deleteTwins(Collection<UUID> twinIds) throws ServiceException {
        entitySmartService.deleteAllAndLog(twinIds, twinRepository);// all linked data will be deleted by fk cascading
    }

    public void loadEraserTransactionScope(TwinEntity twinEntity, TwinEraserTransactionEntity transactionEntity) throws ServiceException {
        twinflowService.loadTwinflow(twinEntity);
        twinEraserTransactionScopeRepository.save(
                new TwinEraserTransactionScopeEntity()
                        .setTwinId(twinEntity.getId())
                        .setSelfScopeLoaded(true)
                        .setTwinEraserTransactionId(transactionEntity.getId())
                        .setReasonTwinTd(twinEntity.getId())
                        .setReason(TwinEraserTransactionScopeEntity.Reason.TARGET)
                        .setEraseTwinStatusId(twinEntity.getTwinflow().getEraseTwinStatusId())
        );
        twinEraserTransactionScopeRepository.addChildTwins(transactionEntity.getId(), twinEntity.getId());
        twinEraserTransactionScopeRepository.addLinked(transactionEntity.getId(), twinEntity.getId());

        /* scope is not fully loaded because:
        1. linked twins can also have children and links
        2. child twins can also have links
        scope will be loaded when all items will have selfScopeLoaded = true
        */
        List<TwinEraserTransactionScopeEntity> selfScopeYetNotLoadedList = twinEraserTransactionScopeRepository.findByTwinEraserTransactionIdAndSelfScopeLoadedFalse(transactionEntity.getId());
        //todo limit loop count
        while (CollectionUtils.isNotEmpty(selfScopeYetNotLoadedList)) {
            twinflowService.loadTwinflow(selfScopeYetNotLoadedList.stream().map(TwinEraserTransactionScopeEntity::getTwin).toList());
            for (TwinEraserTransactionScopeEntity cascadeErase : selfScopeYetNotLoadedList) {
                switch (cascadeErase.getReason()) {
                    case LINK:
                        twinEraserTransactionScopeRepository.addChildTwins(transactionEntity.getId(), cascadeErase.getTwinId());
                        twinEraserTransactionScopeRepository.addLinked(transactionEntity.getId(), cascadeErase.getTwinId());
                        break;
                    case CHILD:
                        twinEraserTransactionScopeRepository.addLinked(transactionEntity.getId(), cascadeErase.getTwinId());
                        break;
                    default:
                        throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "something went wrong");
                }
                cascadeErase
                        .setSelfScopeLoaded(true)
                        .setEraseTwinStatusId(cascadeErase.getTwin().getTwinflow().getEraseTwinStatusId());
                UUID eraseFactoryId = cascadeErase.getTwin().getTwinflow().getEraseTwinFactoryId();
                if (eraseFactoryId == null)
                    continue;
                FactoryContext factoryContext = new FactoryContext()
                        .addInputTwin(cascadeErase.getTwin());
                FactoryResultUncommited factoryResultUncommited = twinFactoryService.runFactory(eraseFactoryId, factoryContext);
            }
            twinEraserTransactionScopeRepository.saveAll(selfScopeYetNotLoadedList);
            selfScopeYetNotLoadedList = twinEraserTransactionScopeRepository.findByTwinEraserTransactionIdAndSelfScopeLoadedFalse(transactionEntity.getId());
        }
    }
}

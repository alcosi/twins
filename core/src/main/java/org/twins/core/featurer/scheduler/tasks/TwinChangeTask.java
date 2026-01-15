package org.twins.core.featurer.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

@Component
@Scope("prototype")
@Slf4j
public class TwinChangeTask implements Runnable {
    private final TwinChangeTaskEntity twinChangeTaskEntity;

    @Autowired
    private TwinFactoryService twinFactoryService;
    @Autowired
    private TwinChangeTaskRepository twinChangeTaskRepository;
    @Autowired
    private AuthService authService;


    public TwinChangeTask(TwinChangeTaskEntity twinChangeTaskEntity) {
        this.twinChangeTaskEntity = twinChangeTaskEntity;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logSession(twinChangeTaskEntity.getRequestId());
            LoggerUtils.logController("twinChangeTask$");
            LoggerUtils.logPrefix("CHANGE_TASK[" + twinChangeTaskEntity.getId() + "]:");
            log.info("Performing async twin change run: {}", twinChangeTaskEntity.logDetailed());
            if (twinChangeTaskEntity.getTwin().getTwinClass().getDomainId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "can not detect domain from input " + twinChangeTaskEntity.getTwin().logNormal());
            }
            authService.setThreadLocalApiUser(twinChangeTaskEntity.getTwin().getTwinClass().getDomainId(), twinChangeTaskEntity.getBusinessAccountId(), twinChangeTaskEntity.getCreatedByUserId());
            FactoryContext factoryContext = new FactoryContext(twinChangeTaskEntity.getTwinFactorylauncher(), FactoryBranchId.root(twinChangeTaskEntity.getTwinFactoryId()))
                    .setInputTwinList(Collections.singletonList(twinChangeTaskEntity.getTwin()));
//                    .setFields(transitionContext.getFields())
//                    .setAttachmentCUD(transitionContext.getAttachmentCUD())
//                    .setBasics(transitionContext.getBasics());
            FactoryResultUncommited result = twinFactoryService.runFactoryAndCollectResult(twinChangeTaskEntity.getTwinFactoryId(), factoryContext);
            if (CollectionUtils.isNotEmpty(result.getDeletes())) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "deletes are currently not supported by  tasks");
            }
            for (var twinCreate : result.getCreates() ) {
                twinCreate.setCanTriggerAfterOperationFactory(false);
            }
            for (var twinUpdate : result.getUpdates() ) {
                twinUpdate.setCanTriggerAfterOperationFactory(false);
            }
            twinFactoryService.commitResult(result);
            twinChangeTaskEntity
                    .setStatusId(TwinChangeTaskStatus.DONE)
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (ServiceException e) {
            log.error(e.log());
            twinChangeTaskEntity
                    .setStatusId(TwinChangeTaskStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            twinChangeTaskEntity
                    .setStatusId(TwinChangeTaskStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
            twinChangeTaskRepository.save(twinChangeTaskEntity);
            LoggerUtils.cleanMDC();
        }
    }

}

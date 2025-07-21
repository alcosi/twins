package org.twins.core.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTaskEntity;
import org.twins.core.dao.factory.TwinFactoryTaskRepository;
import org.twins.core.dao.factory.TwinFactoryTaskStatus;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryLauncher;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;

@Component
@Scope("prototype")
@Slf4j
public class FactoryTask implements Runnable {
    private final TwinFactoryTaskEntity factoryTaskEntity;

    @Autowired
    private TwinFactoryService twinFactoryService;
    @Autowired
    private FactoryTaskService factoryTaskService;
    @Autowired
    private TwinFactoryTaskRepository  twinFactoryTaskRepository;
    @Autowired
    private AuthService authService;


    public FactoryTask(TwinFactoryTaskEntity factoryTaskEntity) {
        this.factoryTaskEntity = factoryTaskEntity;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("factoryTask$");
            LoggerUtils.logPrefix("FACTORY_TASK[" + factoryTaskEntity.getId() + "]:");
            log.info("Performing async factory run: {}", factoryTaskEntity.logNormal());
            if (factoryTaskEntity.getInputTwin().getTwinClass().getDomainId() == null) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "can not detect domain from input " + factoryTaskEntity.getInputTwin().logNormal());
            }
            authService.setThreadLocalApiUser(factoryTaskEntity.getInputTwin().getTwinClass().getDomainId(), factoryTaskEntity.getBusinessAccountId(), factoryTaskEntity.getCreatedByUserId());
            FactoryContext factoryContext = new FactoryContext(FactoryLauncher.beforeTwinUpdate, FactoryBranchId.root(factoryTaskEntity.getTwinFactoryId()))
                    .setInputTwinList(Collections.singletonList(factoryTaskEntity.getInputTwin()));
//                    .setFields(transitionContext.getFields())
//                    .setAttachmentCUD(transitionContext.getAttachmentCUD())
//                    .setBasics(transitionContext.getBasics());
            FactoryResultUncommited result = twinFactoryService.runFactoryAndCollectResult(factoryTaskEntity.getTwinFactoryId(), factoryContext);
            if (CollectionUtils.isNotEmpty(result.getDeletes())) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT, "deletes are currently not supported by factoru tasks");
            }
            twinFactoryService.commitResult(result);
            factoryTaskEntity
                    .setStatusId(TwinFactoryTaskStatus.DONE)
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (ServiceException e) {
            log.error(e.log());
            factoryTaskEntity
                    .setStatusId(TwinFactoryTaskStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            factoryTaskEntity
                    .setStatusId(TwinFactoryTaskStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            authService.removeThreadLocalApiUser();
            twinFactoryTaskRepository.save(factoryTaskEntity);
            LoggerUtils.cleanMDC();
        }
    }

}

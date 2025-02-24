package org.twins.core.service.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class FactoryPipelineService extends EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> {
    @Getter
    private final TwinFactoryPipelineRepository repository;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinFactoryService twinFactoryService;
    private final FactoryConditionSetService factoryConditionSetService;
    private final TwinStatusService twinStatusService;
    private final TwinService twinService;

    @Override
    public CrudRepository<TwinFactoryPipelineEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryPipelineEntity, UUID> entityGetIdFunction() {
        return TwinFactoryPipelineEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryPipelineEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getTwinFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryPipelineEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getInputTwinClass() == null || !entity.getInputTwinClass().getId().equals(entity.getInputTwinClassId()))
                    entity.setInputTwinClass(twinClassService.findEntitySafe(entity.getInputTwinClassId()));
                if (entity.getTwinFactory() == null || !entity.getTwinFactory().getId().equals(entity.getTwinFactoryId()))
                    entity.setTwinFactory(twinFactoryService.findEntitySafe(entity.getTwinFactoryId()));
                if (entity.getTwinFactoryConditionSetId() != null && (entity.getNextTwinFactory() == null || !entity.getNextTwinFactory().getId().equals(entity.getNextTwinFactoryId())))
                    entity.setNextTwinFactory(twinFactoryService.findEntitySafe(entity.getNextTwinFactoryId()));
                if (entity.getTwinFactoryConditionSetId() != null && (entity.getConditionSet() == null || !entity.getConditionSet().getId().equals(entity.getTwinFactoryConditionSetId())))
                    entity.setConditionSet(factoryConditionSetService.findEntitySafe(entity.getTwinFactoryConditionSetId()));
                if (entity.getOutputTwinStatusId() != null && (entity.getOutputTwinStatus() == null || !entity.getOutputTwinStatus().getId().equals(entity.getOutputTwinStatusId())))
                    entity.setOutputTwinStatus(twinStatusService.findEntitySafe(entity.getOutputTwinStatusId()));
                if (entity.getTemplateTwinId() != null && (entity.getTemplateTwin() == null || !entity.getTemplateTwin().getId().equals(entity.getTemplateTwinId())))
                    entity.setTemplateTwin(twinService.findEntitySafe(entity.getTemplateTwinId()));
        }
        return true;
    }

    public TwinFactoryPipelineEntity createFactoryPipeline(TwinFactoryPipelineEntity entity) throws ServiceException {
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryPipelineEntity updateFactoryPipeline(TwinFactoryPipelineEntity entity) throws ServiceException {
        TwinFactoryPipelineEntity dbEntity = findEntitySafe(entity.getId());
        entity.setId(dbEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateInputTwinClassId(dbEntity, entity.getInputTwinClassId(), changesHelper);
        updateFactoryConditionSetId(dbEntity, entity.getTwinFactoryConditionSetId(), changesHelper);
        updateFactoryConditionSetInvert(dbEntity, entity.getTwinFactoryConditionInvert(), changesHelper);
        updateActive(dbEntity, entity.getActive(), changesHelper);
        updateOutputStatusId(dbEntity, entity.getOutputTwinStatusId(), changesHelper);
        updateNextFactoryId(dbEntity, entity.getNextTwinFactoryId(), changesHelper);
        updateTemplateTwinId(dbEntity, entity.getTemplateTwinId(), changesHelper);
        updateDescription(dbEntity, entity.getDescription(), changesHelper);

        return updateSafe(dbEntity, changesHelper);
    }

    private void updateInputTwinClassId(TwinFactoryPipelineEntity dbEntity, UUID newInputTwinClassId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.inputTwinClassId, dbEntity.getInputTwinClassId(), newInputTwinClassId))
            return;
        dbEntity.setInputTwinClassId(newInputTwinClassId);
    }

    private void updateFactoryConditionSetId(TwinFactoryPipelineEntity dbEntity, UUID newFactoryConditionSetId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.twinFactoryConditionSetId, dbEntity.getTwinFactoryConditionSetId(), newFactoryConditionSetId))
            return;
        dbEntity.setTwinFactoryConditionSetId(newFactoryConditionSetId);
    }

    private void updateFactoryConditionSetInvert(TwinFactoryPipelineEntity dbEntity, Boolean newFactoryConditionSetInvert,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.twinFactoryConditionInvert, dbEntity.getTwinFactoryConditionInvert(), newFactoryConditionSetInvert))
            return;
        dbEntity.setTwinFactoryConditionInvert(newFactoryConditionSetInvert);
    }

    private void updateActive(TwinFactoryPipelineEntity dbEntity, Boolean newActive,
                              ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.active, dbEntity.getActive(), newActive))
            return;
        dbEntity.setActive(newActive);
    }

    private void updateOutputStatusId(TwinFactoryPipelineEntity dbEntity, UUID newOutputStatusId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.outputTwinStatusId, dbEntity.getOutputTwinStatusId(), newOutputStatusId))
            return;
        dbEntity.setOutputTwinStatusId(newOutputStatusId);
    }

    private void updateNextFactoryId(TwinFactoryPipelineEntity dbEntity, UUID newNextFactoryId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.nextTwinFactoryId, dbEntity.getNextTwinFactoryId(), newNextFactoryId))
            return;
        dbEntity.setNextTwinFactoryId(newNextFactoryId);
    }

    private void updateTemplateTwinId(TwinFactoryPipelineEntity dbEntity, UUID newTemplateTwinId,
                                        ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.templateTwinId, dbEntity.getTemplateTwinId(), newTemplateTwinId))
            return;
        dbEntity.setTemplateTwinId(newTemplateTwinId);
    }

    private void updateDescription(TwinFactoryPipelineEntity dbEntity, String newDescription,
                                   ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryPipelineEntity.Fields.description, dbEntity.getDescription(), newDescription))
            return;
        dbEntity.setDescription(newDescription);
    }
}

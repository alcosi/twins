package org.twins.core.service.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.controller.rest.priv.factory.TwinFactoryConditionSetService;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dao.factory.TwinFactoryEraserRepository;
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
    private final TwinFactoryConditionSetService twinFactoryConditionSetService;
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
                if (entity.getNextTwinFactory() == null || !entity.getNextTwinFactory().getId().equals(entity.getNextTwinFactoryId()))
                    entity.setNextTwinFactory(twinFactoryService.findEntitySafe(entity.getNextTwinFactoryId()));
                if (entity.getConditionSet() == null || !entity.getConditionSet().getId().equals(entity.getTwinFactoryConditionSetId()))
                    entity.setConditionSet(twinFactoryConditionSetService.findEntitySafe(entity.getTwinFactoryConditionSetId()));
                if (entity.getOutputTwinStatus() == null || !entity.getOutputTwinStatus().getId().equals(entity.getOutputTwinStatusId()))
                    entity.setOutputTwinStatus(twinStatusService.findEntitySafe(entity.getOutputTwinStatusId()));
                if (entity.getTemplateTwin() == null || !entity.getTemplateTwin().getId().equals(entity.getTemplateTwinId()))
                    entity.setTemplateTwin(twinService.findEntitySafe(entity.getTemplateTwinId()));
        }
        return true;
    }

    public TwinFactoryPipelineEntity createFactoryPipeline(TwinFactoryPipelineEntity entity) throws ServiceException {
        entity.setId(UUID.randomUUID());
        validateEntityAndThrow(entity, EntitySmartService.EntityValidateMode.beforeSave);
        return repository.save(entity);
    }
}

package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.*;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldPlugService extends EntitySecureFindServiceImpl<TwinClassFieldPlugEntity> {

    private final TwinClassFieldPlugRepository twinClassFieldPlugRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassFieldRepository twinClassFieldRepository;

    @Override
    public CrudRepository<TwinClassFieldPlugEntity, UUID> entityRepository() {
        return twinClassFieldPlugRepository;
    }

    @Override
    public Function<TwinClassFieldPlugEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldPlugEntity::getTwinClassFieldId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldPlugEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldPlugEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave -> {
                   if (entity.getTwinClassId() == null || entity.getTwinClassFieldId() == null) {
                       return logErrorAndReturnFalse(ErrorCodeTwins.ENTITY_INVALID.getMessage());
                   }

                   if (!twinClassRepository.existsById(entity.getTwinClassId())) {
                       return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN.getMessage());
                   }

                   Optional<TwinClassFieldEntity> fieldOptional = twinClassFieldRepository.findById(entity.getTwinClassFieldId());
                   if (fieldOptional.isEmpty()) {
                       return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_ID_UNKNOWN.getMessage());
                   } else if (fieldOptional.get().getTwinClassFieldVisibilityId() != TwinClassFieldEntity.TwinClassFieldVisibility.PLUGGABLE) {
                       return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_IS_NOT_PLUGGABLE.getMessage());
                   }
            }
        }

        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldPlugEntity plugField(TwinClassFieldPlugEntity entity) throws ServiceException {
        return plugFields(List.of(entity)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldPlugEntity> plugFields(Collection<TwinClassFieldPlugEntity> entities) throws ServiceException {
        return StreamSupport.stream(saveSafe(entities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unplugField(TwinClassFieldPlugEntity entity) throws ServiceException {
        unplugFields(List.of(entity));
    }

    @Transactional(rollbackFor = Throwable.class)
    public void unplugFields(Collection<TwinClassFieldPlugEntity> entities) throws ServiceException {
//        deleteSafe(entities.stream());
    }
}

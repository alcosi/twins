package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerRepository;
import org.twins.core.domain.twin.TwinPointerCreate;
import org.twins.core.domain.twin.TwinPointerUpdate;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinPointerService extends EntitySecureFindServiceImpl<TwinPointerEntity> {
    private final TwinPointerRepository twinPointerRepository;
    private final FeaturerService featurerService;
    private final TwinClassService twinClassService;
    private final AuthService authService;
    private final UserService userService;

    @Override
    public CrudRepository<TwinPointerEntity, UUID> entityRepository() {
        return twinPointerRepository;
    }

    @Override
    public Function<TwinPointerEntity, UUID> entityGetIdFunction() {
        return TwinPointerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinPointerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return checkDomainAccessDenied(entity.getDomainId(), entity.logNormal(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinPointerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getPointerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty pointerFeaturerId");
        // pointerFeaturerId must resolve to a Pointer featurer
        featurerService.getFeaturer(entity.getPointerFeaturerId(), Pointer.class);
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinPointerEntity> createTwinPointers(List<TwinPointerCreate> createList) throws ServiceException {
        UUID apiUserId = authService.getApiUser().getUserId();
        UUID domainId = authService.getApiUser().getDomainId();
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        List<TwinPointerEntity> entities = new ArrayList<>();
        for (TwinPointerCreate create : createList) {
            TwinPointerEntity entity = create.getTwinPointer()
                    .setId(UUID.randomUUID())
                    .setCreatedAt(now)
                    .setCreatedByUserId(apiUserId)
                    .setDomainId(domainId);
            entities.add(entity);
        }
        List<TwinPointerEntity> result = new ArrayList<>();
        for (TwinPointerEntity saved : saveSafe(entities)) {
            result.add(saved);
        }
        return result;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinPointerEntity> updateTwinPointers(List<TwinPointerUpdate> updateList) throws ServiceException {
        List<TwinPointerEntity> result = new ArrayList<>();
        for (TwinPointerUpdate update : updateList) {
            TwinPointerEntity dbEntity = findEntitySafe(update.getTwinPointer().getId());
            TwinPointerEntity src = update.getTwinPointer();
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByEntity(src, dbEntity, TwinPointerEntity::getTwinClassId, TwinPointerEntity::setTwinClassId, TwinPointerEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByEntity(src, dbEntity, TwinPointerEntity::getPointerFeaturerId, TwinPointerEntity::setPointerFeaturerId, TwinPointerEntity.Fields.pointerFeaturerId, changesHelper);
            updateEntityFieldByEntity(src, dbEntity, TwinPointerEntity::getPointerParams, TwinPointerEntity::setPointerParams, TwinPointerEntity.Fields.pointerParams, changesHelper);
            updateEntityFieldByEntity(src, dbEntity, TwinPointerEntity::getName, TwinPointerEntity::setName, TwinPointerEntity.Fields.name, changesHelper);
            updateEntityFieldByEntity(src, dbEntity, TwinPointerEntity::getOptional, TwinPointerEntity::setOptional, TwinPointerEntity.Fields.optional, changesHelper);
            result.add(updateSafe(dbEntity, changesHelper));
        }
        return result;
    }

    public void loadTwinClass(TwinPointerEntity pointer) throws ServiceException {
        loadTwinClass(Collections.singleton(pointer));
    }

    public void loadTwinClass(Collection<TwinPointerEntity> pointers) throws ServiceException {
        twinClassService.load(pointers,
                TwinPointerEntity::getTwinClassId,
                TwinPointerEntity::getTwinClass,
                TwinPointerEntity::setTwinClass);
    }

    public void loadCreatedByUser(TwinPointerEntity pointer) throws ServiceException {
        loadCreatedByUser(Collections.singleton(pointer));
    }

    public void loadCreatedByUser(Collection<TwinPointerEntity> pointers) throws ServiceException {
        userService.load(pointers,
                TwinPointerEntity::getCreatedByUserId,
                TwinPointerEntity::getCreatedByUser,
                TwinPointerEntity::setCreatedByUser);
    }

    public TwinEntity getPointer(TwinEntity currentTwin, UUID twinPointerId) throws ServiceException {
        TwinPointerEntity twinPointer = findEntitySafe(twinPointerId);
        Pointer pointer = featurerService.getFeaturer(twinPointer.getPointerFeaturerId(), Pointer.class);
        return pointer.point(twinPointer, currentTwin);
    }


    public void loadPointer(Collection<TwinEntity> srcTwins, UUID twinPointerId) throws ServiceException {
        loadPointer(srcTwins, findEntitySafe(twinPointerId));
    }

    public void loadPointer(Collection<TwinEntity> srcTwins, TwinPointerEntity twinPointer) throws ServiceException {
        Pointer pointer = featurerService.getFeaturer(twinPointer.getPointerFeaturerId(), Pointer.class);
        pointer.load(twinPointer, srcTwins);
    }

    public Map<UUID, TwinEntity> getPointers(Collection<TwinEntity> srcTwins, UUID twinPointerId) throws ServiceException {
        TwinPointerEntity twinPointer = findEntitySafe(twinPointerId);
        Pointer pointer = featurerService.getFeaturer(twinPointer.getPointerFeaturerId(), Pointer.class);
        pointer.load(twinPointer, srcTwins);
        Map<UUID, TwinEntity> result = new HashMap<>();
        for (TwinEntity src : srcTwins) {
            TwinEntity pointed = src.getPointer(twinPointerId);
            if (pointed != null) {
                result.put(src.getId(), pointed);
            }
        }
        return result;
    }
}

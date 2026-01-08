package org.twins.core.service.eraseflow;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.eraseflow.EraseflowEntity;
import org.twins.core.dao.eraseflow.EraseflowLinkCascadeEntity;
import org.twins.core.dao.eraseflow.EraseflowLinkCascadeRepository;
import org.twins.core.dao.eraseflow.EraseflowRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class EraseflowService extends EntitySecureFindServiceImpl<EraseflowEntity> {
    private final EraseflowRepository eraseflowRepository;
    private final EraseflowLinkCascadeRepository eraseflowLinkCascadeRepository;
    @Lazy
    private final TwinflowService twinflowService;


    @Override
    public CrudRepository<EraseflowEntity, UUID> entityRepository() {
        return eraseflowRepository;
    }

    @Override
    public Function<EraseflowEntity, UUID> entityGetIdFunction() {
        return EraseflowEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(EraseflowEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(EraseflowEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public EraseflowEntity loadEraseflow(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinflow() != null && twinEntity.getTwinflow().getEraseflow() != null)
            return twinEntity.getTwinflow().getEraseflow();
        twinflowService.loadTwinflow(twinEntity);
        return loadEraseflow(twinEntity.getTwinflow());
    }

    public EraseflowEntity loadEraseflow(TwinflowEntity twinflowEntity) throws ServiceException {
        if (twinflowEntity.getEraseflow() != null || twinflowEntity.getEraseflowId() == null)
            return twinflowEntity.getEraseflow();
        twinflowEntity.setEraseflow(findEntitySafe(twinflowEntity.getEraseflowId()));
        return twinflowEntity.getEraseflow();
    }

    public void loadEraseflow(Collection<TwinEntity> collection) throws ServiceException {
        twinflowService.loadTwinflow(collection);
        KitGrouped<TwinflowEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinflowEntity::getId, TwinflowEntity::getEraseflowId);
        for (TwinEntity twinEntity : collection) {
            if (twinEntity.getTwinflow().getEraseflow() == null
                    && twinEntity.getTwinflow().getEraseflowId() != null
                    && !needLoad.containsKey(twinEntity.getTwinflow().getId()))
                needLoad.add(twinEntity.getTwinflow());
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        Kit<EraseflowEntity, UUID> loaded = findEntitiesSafe(needLoad.getGroupedMap().keySet());
        EraseflowEntity eraseflowEntity = null;
        for (TwinflowEntity twinflowEntity : needLoad.getCollection()) {
            eraseflowEntity = loaded.get(twinflowEntity.getEraseflowId());
            if (eraseflowEntity == null)
                throw new ServiceException(ErrorCodeTwins.TWINFLOW_ERASEFLOW_INCORRECT, "can not load eraseflow by id " + twinflowEntity.getEraseflowId());
            twinflowEntity.setEraseflow(loadEraseflow(twinflowEntity));
        }
    }

    public void loadEraseflowLinkCascade(EraseflowEntity eraseflow) {
        if (eraseflow.getCascadeLinkKit() != null)
            return;
        // here we use EraseflowLinkCascadeEntity::getLinkId, but not EraseflowLinkCascadeEntity::getId, because we have complex uniq key
        eraseflow.setCascadeLinkKit(new Kit<>(eraseflowLinkCascadeRepository.findByEraseflowId(eraseflow.getId()), EraseflowLinkCascadeEntity::getLinkId));
    }
}


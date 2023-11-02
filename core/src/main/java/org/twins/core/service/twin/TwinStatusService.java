package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;
import java.util.UUID;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatusService extends EntitySecureFindServiceImpl<TwinStatusEntity> {
    final TwinStatusRepository twinStatusRepository;
    final TwinClassService twinClassService;
    @Override
    public CrudRepository<TwinStatusEntity, UUID> entityRepository() {
        return twinStatusRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatusEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatusEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinStatusEntity> findByTwinClass(TwinClassEntity twinClassEntity) {
        twinClassService.loadExtendedClasses(twinClassEntity);
        return twinStatusRepository.findByTwinClassIdIn(twinClassEntity.getExtendedClassIdSet());
    }
}

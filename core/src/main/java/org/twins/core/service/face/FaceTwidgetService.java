package org.twins.core.service.face;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceTwidgetEntity;
import org.twins.core.domain.face.TwidgetConfig;

import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public abstract class FaceTwidgetService<T extends FaceTwidgetEntity> extends EntitySecureFindServiceImpl<T> {

    public abstract TwidgetConfig<T> getConfig(UUID faceId, UUID currentTwinId) throws ServiceException;


}

package org.twins.core.service.face;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceTwidget;
import org.twins.core.dao.face.FaceTwidgetEntity;
import org.twins.core.dao.face.FaceTwidgetRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Slf4j
@Service
@Lazy
public abstract class FaceSelectorService<T extends FaceTwidget> extends EntitySecureFindServiceImpl<T> {
    @Autowired
    private FaceTwidgetRepository faceTwidgetRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private TwinService twinService;

    public TwidgetConfig<T> getConfig(UUID faceId, UUID currentTwinId) throws ServiceException {
        TwinEntity currentTwin = twinService.findEntitySafe(currentTwinId);
        FaceTwidgetEntity faceTwidgetEntity = faceTwidgetRepository.findByFaceId(faceId);
        TwinEntity targetTwin = null;
        if (faceTwidgetEntity == null) {
            targetTwin = currentTwin;
        } else {
            Pointer pointer = featurerService.getFeaturer(faceTwidgetEntity.getPointerFeaturer(), Pointer.class);
            targetTwin = pointer.point(faceTwidgetEntity.getPointerParams(), currentTwin);
        }
        TwidgetConfig<T> ret = new TwidgetConfig<>();
        ret
                .setTargetTwinId(targetTwin.getId()) //should be replaced in future
                .setConfig(getConfig(faceId, currentTwin, targetTwin));
        return ret;
    }

    public abstract T getConfig(UUID faceId, TwinEntity currentTwin, TwinEntity targetTwin) throws ServiceException;
}

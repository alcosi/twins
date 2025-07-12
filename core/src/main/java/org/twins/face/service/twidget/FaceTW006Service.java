package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.TwinActionRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionRepository;
import org.twins.face.dao.twidget.tw006.FaceTW006Entity;
import org.twins.face.dao.twidget.tw006.FaceTW006Repository;
import org.twins.face.dto.rest.twidget.tw006.FaceTW006ActionDTOv1;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW006Service extends FacePointedService<FaceTW006Entity> {
    private final FaceTW006Repository faceTW006Repository;
    private final FaceTW006ActionRepository faceTW006ActionRepository;
    private final TwinActionRepository twinActionRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW006Entity, UUID> entityRepository() {
        return faceTW006Repository;
    }

    @Override
    public Function<FaceTW006Entity, UUID> entityGetIdFunction() {
        return FaceTW006Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW006Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW006Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW006Entity> getVariants(UUID faceId) {
        return faceTW006Repository.findByFaceId(faceId);
    }

    public FaceTW006ActionDTOv1 loadActionDTO(PointedFace<FaceTW006Entity> src) {
        FaceTW006ActionDTOv1 ret = new FaceTW006ActionDTOv1();
        UUID tw006Id = src.getConfig().getId();
        FaceTW006ActionEntity actionEntity = faceTW006ActionRepository.findByFaceTW006Id(tw006Id);

        ret
                .setActionId(actionEntity.getTwinActionId())
                .setFaceTW006Id(tw006Id)
                .setLabelI18n(
                        actionEntity.getLabelI18n() == null ?
                                twinActionRepository.findById(actionEntity.getTwinActionId()).getI18nEntity() :
                                actionEntity.getLabelI18n()
                );

        return ret;
    }
}

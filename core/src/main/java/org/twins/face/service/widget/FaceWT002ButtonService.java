package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonEntity;
import org.twins.face.dao.widget.wt002.FaceWT002ButtonRepository;
import org.twins.face.dao.widget.wt002.FaceWT002Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT002ButtonService extends FaceVariantsService<FaceWT002ButtonEntity> {
    private final FaceWT002ButtonRepository faceWT002ButtonRepository;

    @Override
    public CrudRepository<FaceWT002ButtonEntity, UUID> entityRepository() {
        return faceWT002ButtonRepository;
    }

    @Override
    public Function<FaceWT002ButtonEntity, UUID> entityGetIdFunction() {
        return FaceWT002ButtonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT002ButtonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT002ButtonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadButtons(FaceWT002Entity src) {
        loadButtons(Collections.singletonList(src));
    }

    public void loadButtons(Collection<FaceWT002Entity> srcList) {
        loadKit(srcList,
                FaceWT002Entity::getId,
                FaceWT002Entity::getButtons,
                FaceWT002Entity::setButtons,
                faceWT002ButtonRepository::findByFaceWT002IdIn,
                FaceWT002ButtonEntity::getId,
                FaceWT002ButtonEntity::getFaceWT002Id,
                FaceWT002ButtonEntity::setFaceWT002);
    }

    @Override
    public List<FaceWT002ButtonEntity> getVariants(UUID faceWT002Id) {
        return faceWT002ButtonRepository.findByFaceWT002Id(faceWT002Id);
    }
}
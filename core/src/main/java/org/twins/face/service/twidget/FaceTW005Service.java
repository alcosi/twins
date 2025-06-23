package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.twidget.tw005.FaceTW005ButtonEntity;
import org.twins.face.dao.twidget.tw005.FaceTW005ButtonRepository;
import org.twins.face.dao.twidget.tw005.FaceTW005Entity;
import org.twins.face.dao.twidget.tw005.FaceTW005Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW005Service extends FaceVariantsService<FaceTW005Entity> {
    private final FaceTW005Repository faceTW005Repository;
    private final FaceTW005ButtonRepository faceTW005ButtonRepository;

    @Override
    public CrudRepository<FaceTW005Entity, UUID> entityRepository() {
        return faceTW005Repository;
    }

    @Override
    public Function<FaceTW005Entity, UUID> entityGetIdFunction() {
        return FaceTW005Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW005Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTW005Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW005Entity> getVariants(UUID of) {
        return faceTW005Repository.findByFaceId(of);
    }

    public void loadButtons(FaceTW005Entity src) {
        loadButtons(Collections.singletonList(src));
    }

    public void loadButtons(Collection<FaceTW005Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceTW005Entity, UUID> needLoad = new Kit<>(FaceTW005Entity::getId);
        for (var faceTW005Entity : srcList)
            if (faceTW005Entity.getButtons() == null) {
                faceTW005Entity.setButtons(new Kit<>(FaceTW005ButtonEntity::getId));
                needLoad.add(faceTW005Entity);
            }
        if (needLoad.isEmpty())
            return;
        List<FaceTW005ButtonEntity> buttons = faceTW005ButtonRepository.findByFaceTW005IdIn(needLoad.getIdSet());
        KitGrouped<FaceTW005ButtonEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                buttons, FaceTW005ButtonEntity::getId, FaceTW005ButtonEntity::getFaceTW005Id);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getButtons().addAll(entry.getValue());
        }
    }
}
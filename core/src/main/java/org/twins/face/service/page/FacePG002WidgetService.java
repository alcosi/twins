package org.twins.face.service.page;

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
import org.twins.face.dao.page.pg002.FacePG002TabEntity;
import org.twins.face.dao.page.pg002.FacePG002WidgetEntity;
import org.twins.face.dao.page.pg002.FacePG002WidgetRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePG002WidgetService extends FaceVariantsService<FacePG002WidgetEntity> {
    private final FacePG002WidgetRepository facePG002WidgetRepository;

    @Override
    public CrudRepository<FacePG002WidgetEntity, UUID> entityRepository() {
        return facePG002WidgetRepository;
    }

    @Override
    public Function<FacePG002WidgetEntity, UUID> entityGetIdFunction() {
        return FacePG002WidgetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePG002WidgetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FacePG002WidgetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadWidgets(FacePG002TabEntity src) {
        loadWidgets(Collections.singletonList(src));
    }

    public void loadWidgets(Collection<FacePG002TabEntity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FacePG002TabEntity, UUID> needLoad = new Kit<>(FacePG002TabEntity::getId);
        for (var facePG001Entity : srcList)
            if (facePG001Entity.getWidgets() == null) {
                facePG001Entity.setWidgets(new Kit<>(FacePG002WidgetEntity::getId));
                needLoad.add(facePG001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FacePG002WidgetEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                facePG002WidgetRepository.findByFacePagePG002TabIdInAndActiveTrue(needLoad.getIdSet()), FacePG002WidgetEntity::getId, FacePG002WidgetEntity::getFacePagePG002TabId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getWidgets().addAll(entry.getValue());
        }
    }

    @Override
    public List<FacePG002WidgetEntity> getVariants(UUID of) {
        return facePG002WidgetRepository.findByFacePagePG002TabIdInAndActiveTrue(Collections.singleton(of));
    }
}
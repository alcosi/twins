package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.face.dao.widget.FaceWT004AccordionItemEntity;
import org.twins.face.dao.widget.FaceWT004AccordionItemRepository;
import org.twins.face.dao.widget.FaceWT004Entity;
import org.twins.face.dao.widget.FaceWT004Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT004Service extends EntitySecureFindServiceImpl<FaceWT004Entity> {
    private final FaceWT004Repository faceWT004Repository;
    private final FaceWT004AccordionItemRepository faceWT004AccordionItemRepository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<FaceWT004Entity, UUID> entityRepository() {
        return faceWT004Repository;
    }

    @Override
    public Function<FaceWT004Entity, UUID> entityGetIdFunction() {
        return FaceWT004Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT004Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getFace().getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT004Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadAccordionItems(FaceWT004Entity src) {
        loadAccordionItems(Collections.singletonList(src));
    }

    public void loadAccordionItems(Collection<FaceWT004Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceWT004Entity, UUID> needLoad = new Kit<>(FaceWT004Entity::getFaceId);
        for (var faceNB001Entity : srcList)
            if (faceNB001Entity.getAccordionItems() == null) {
                faceNB001Entity.setAccordionItems(new Kit<>(FaceWT004AccordionItemEntity::getId));
                needLoad.add(faceNB001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceWT004AccordionItemEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceWT004AccordionItemRepository.findByFaceIdIn(needLoad.getIdSet()), FaceWT004AccordionItemEntity::getId, FaceWT004AccordionItemEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getAccordionItems().addAll(entry.getValue());
        }
    }
}

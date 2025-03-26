package org.twins.face.service.twidget;

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
import org.twins.face.dao.twiget.FaceTW002AccordionItemEntity;
import org.twins.face.dao.twiget.FaceTW002AccordionItemRepository;
import org.twins.face.dao.twiget.FaceTW002Entity;
import org.twins.face.dao.twiget.FaceTW002Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW002Service extends EntitySecureFindServiceImpl<FaceTW002Entity> {
    private final FaceTW002Repository faceTW002Repository;
    private final FaceTW002AccordionItemRepository faceTW002AccordionItemRepository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<FaceTW002Entity, UUID> entityRepository() {
        return faceTW002Repository;
    }

    @Override
    public Function<FaceTW002Entity, UUID> entityGetIdFunction() {
        return FaceTW002Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getFace().getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceTW002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadAccordionItems(FaceTW002Entity src) {
        loadAccordionItems(Collections.singletonList(src));
    }

    public void loadAccordionItems(Collection<FaceTW002Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceTW002Entity, UUID> needLoad = new Kit<>(FaceTW002Entity::getFaceId);
        for (var faceNB001Entity : srcList)
            if (faceNB001Entity.getAccordionItems() == null) {
                faceNB001Entity.setAccordionItems(new Kit<>(FaceTW002AccordionItemEntity::getId));
                needLoad.add(faceNB001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceTW002AccordionItemEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceTW002AccordionItemRepository.findByFaceIdIn(needLoad.getIdSet()), FaceTW002AccordionItemEntity::getId, FaceTW002AccordionItemEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getAccordionItems().addAll(entry.getValue());
        }
    }
}

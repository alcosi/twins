package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.twidget.tw002.FaceTW002AccordionItemEntity;
import org.twins.face.dao.twidget.tw002.FaceTW002AccordionItemRepository;
import org.twins.face.dao.twidget.tw002.FaceTW002Entity;
import org.twins.face.dao.twidget.tw002.FaceTW002Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW002Service extends FacePointedService<FaceTW002Entity> {
    private final FaceTW002Repository faceTW002Repository;
    private final FaceTW002AccordionItemRepository faceTW002AccordionItemRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTW002Entity, UUID> entityRepository() {
        return faceTW002Repository;
    }

    @Override
    public Function<FaceTW002Entity, UUID> entityGetIdFunction() {
        return FaceTW002Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW002Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceTW002Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadAccordionItems(FaceTW002Entity src) {
        loadAccordionItems(Collections.singletonList(src));
    }

    public void loadAccordionItems(Collection<FaceTW002Entity> srcList) {
        loadKit(srcList,
                FaceTW002Entity::getId,
                FaceTW002Entity::getAccordionItems,
                FaceTW002Entity::setAccordionItems,
                faceTW002AccordionItemRepository::findByFaceTW002IdIn,
                FaceTW002AccordionItemEntity::getId,
                FaceTW002AccordionItemEntity::getFaceTW002Id,
                FaceTW002AccordionItemEntity::setFaceTW002);
    }

    @Override
    public List<FaceTW002Entity> getVariants(UUID of) {
        return faceTW002Repository.findByFaceId(of);
    }
}
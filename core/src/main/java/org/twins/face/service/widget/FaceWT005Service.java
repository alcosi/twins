package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FacePointedService;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;
import org.twins.face.dao.widget.wt005.FaceWT005AccordionItemEntity;
import org.twins.face.dao.widget.wt005.FaceWT005AccordionItemRepository;
import org.twins.face.dao.widget.wt005.FaceWT005Entity;
import org.twins.face.dao.widget.wt005.FaceWT005Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT005Service extends FacePointedService<FaceWT005Entity> {
    private final FaceWT005Repository faceWT005Repository;
    private final FaceWT005AccordionItemRepository faceWT005AccordionItemRepository;
    private final FaceService faceService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<FaceWT005Entity, UUID> entityRepository() {
        return faceWT005Repository;
    }

    @Override
    public Function<FaceWT005Entity, UUID> entityGetIdFunction() {
        return FaceWT005Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT005Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (entity.getFace() == null)
            entity.setFace(findFaceForReadCheck(entity.getFaceId()));
        return faceService.isEntityReadDenied(entity.getFace(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(FaceWT005Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadAccordionItems(FaceWT005Entity src) {
        loadAccordionItems(Collections.singletonList(src));
    }

    public void loadAccordionItems(Collection<FaceWT005Entity> srcList) {
        loadKit(srcList,
                FaceWT005Entity::getId,
                FaceWT005Entity::getAccordionItems,
                FaceWT005Entity::setAccordionItems,
                faceWT005AccordionItemRepository::findByFaceWT005IdIn,
                FaceWT005AccordionItemEntity::getId,
                FaceWT005AccordionItemEntity::getFaceWT005Id,
                FaceWT005AccordionItemEntity::setFaceWT005);
    }

    @Override
    public List<FaceWT005Entity> getVariants(UUID of) {
        return faceWT005Repository.findByFaceId(of);
    }

    public void loadI18nTwinClassFields(FaceWT005Entity src) throws ServiceException {
        loadI18nTwinClassFields(Collections.singletonList(src));
    }

    public void loadI18nTwinClassFields(Collection<FaceWT005Entity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                FaceWT005Entity::getI18nTwinClassFieldId,
                FaceWT005Entity::getI18nTwinClassField,
                FaceWT005Entity::setI18nTwinClassField);
    }
}

package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceVariantsService;
import org.twins.face.dao.twidget.tw004.FaceTW004Entity;
import org.twins.face.dao.twidget.tw004.FaceTW004Repository;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW004Service extends FaceVariantsService<FaceTW004Entity> {
    private final FaceTW004Repository faceTW004Repository;

    @Override
    public CrudRepository<FaceTW004Entity, UUID> entityRepository() {
        return faceTW004Repository;
    }

    @Override
    public Function<FaceTW004Entity, UUID> entityGetIdFunction() {
        return FaceTW004Entity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW004Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTW004Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public List<FaceTW004Entity> getVariants(UUID faceId) {
        Specification<FaceTW004Entity> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("faceId"), faceId);
        return faceTW004Repository.findAll(spec);
    }
}
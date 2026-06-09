package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.cambium.common.util.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassUniquenessEntity;
import org.twins.core.dao.twinclass.TwinClassUniquenessFieldEntity;
import org.twins.core.dao.twinclass.TwinClassUniquenessFieldRepository;
import org.twins.core.dao.twinclass.TwinClassUniquenessRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TwinClassUniquenessConfigService extends TwinsEntitySecureFindService<TwinClassUniquenessEntity> {

    private final TwinClassUniquenessRepository twinClassUniquenessRepository;
    private final TwinClassUniquenessFieldRepository fieldRepository;
    private final TwinClassService twinClassService;
    private final TwinClassFieldService twinClassFieldService;
    private final AuthService authService;

    @Autowired
    private CacheManager cacheManager;

    public static final String CACHE_UNIQUENESS_BY_TWIN_CLASS_ID = "TwinClassUniquenessConfigService.findByTwinClassId";

    @Override
    public CrudRepository<TwinClassUniquenessEntity, UUID> entityRepository() {
        return twinClassUniquenessRepository;
    }

    @Override
    public Function<TwinClassUniquenessEntity, UUID> entityGetIdFunction() {
        return TwinClassUniquenessEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassUniquenessEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassUniquenessEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Cacheable(value = CACHE_UNIQUENESS_BY_TWIN_CLASS_ID, key = "#twinClassId")
    public Kit<TwinClassUniquenessEntity, UUID> findByTwinClassId(UUID twinClassId) {
        List<TwinClassUniquenessEntity> entities = twinClassUniquenessRepository.findByTwinClassId(twinClassId);
        return new Kit<>(entities, TwinClassUniquenessEntity::getId);
    }

    public void loadUniquenessFields(TwinClassUniquenessEntity uniquenessEntity) {
        loadUniquenessFields(List.of(uniquenessEntity));
    }

    public void loadUniquenessFields(Collection<TwinClassUniquenessEntity> uniquenessEntities) {
        Kit<TwinClassUniquenessEntity, UUID> needLoad = new Kit<>(TwinClassUniquenessEntity::getId);
        uniquenessEntities.stream()
                .filter(e -> e.getFieldKit() == null)
                .forEach(needLoad::add);

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinClassUniquenessFieldEntity, UUID, UUID> fields = new KitGrouped<>(
                fieldRepository.findByTwinClassUniquenessIdIn(needLoad.getIdSet()),
                TwinClassUniquenessFieldEntity::getId,
                TwinClassUniquenessFieldEntity::getTwinClassUniquenessId
        );

        for (TwinClassUniquenessEntity uniqueness : needLoad) {
            if (fields.containsGroupedKey(uniqueness.getId())) {
                uniqueness.setFieldKit(new Kit<>(
                        fields.getGrouped(uniqueness.getId()),
                        TwinClassUniquenessFieldEntity::getId
                ));
            } else {
                uniqueness.setFieldKit(Kit.emptyKit());
            }
        }
    }

    public void validateUniquenessConfig(TwinClassEntity ownerClass, TwinClassUniquenessEntity uniqueness) throws ServiceException {
        if (uniqueness.getFieldKit() == null) {
            loadUniquenessFields(uniqueness);
        }

        Set<UUID> extendedClassIdSet = ownerClass.getExtendedClassIdSet();
        UUID ownerId = ownerClass.getId();

        for (TwinClassUniquenessFieldEntity field : uniqueness.getFieldKit().getCollection()) {
            TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(field.getTwinClassFieldId());
            TwinClassEntity fieldOwnerClass = twinClassField.getTwinClass();

            if (!extendedClassIdSet.contains(fieldOwnerClass.getId())) {
                throw new ServiceException(
                        org.twins.core.exception.ErrorCodeTwins.ENTITY_INVALID,
                        "Field " + twinClassField.logNormal() + " does not belong to extended class set of " + ownerClass.logNormal()
                );
            }

            if (!fieldOwnerClass.getId().equals(ownerId) && !Boolean.TRUE.equals(twinClassField.getInheritable())) {
                throw new ServiceException(
                        org.twins.core.exception.ErrorCodeTwins.ENTITY_INVALID,
                        "Field " + twinClassField.logNormal() + " is not inheritable"
                );
            }

            if (fieldOwnerClass.getExtendedClassIdSet().contains(ownerId) && Boolean.TRUE.equals(uniqueness.getInheritable())) {
                throw new ServiceException(
                        org.twins.core.exception.ErrorCodeTwins.ENTITY_INVALID,
                        "Cannot reference field from descendant class in inheritable uniqueness"
                );
            }
        }
    }

    public void evictCache(UUID twinClassId) throws ServiceException {
        CacheUtils.evictCache(
                cacheManager,
                CACHE_UNIQUENESS_BY_TWIN_CLASS_ID,
                twinClassId
        );
    }
}

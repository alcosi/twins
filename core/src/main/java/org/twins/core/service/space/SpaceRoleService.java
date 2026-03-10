package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.space.SpaceRoleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.space.SpaceRoleCreate;
import org.twins.core.domain.space.SpaceRoleUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleService extends TwinsEntitySecureFindService<SpaceRoleEntity> {
    @Getter
    private final SpaceRoleRepository repository;

    @Lazy
    final EntitySmartService entitySmartService;

    @Lazy
    final AuthService authService;

    final SpaceRoleRepository spaceRoleRepository;
    private final I18nService i18nService;
    private final TwinClassService twinClassService;
    private final BusinessAccountService businessAccountService;

    public void forceDeleteRoles(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        List<UUID> rolesToDelete = spaceRoleRepository.findAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        entitySmartService.deleteAllAndLog(rolesToDelete, spaceRoleRepository);
    }

    @Override
    public CrudRepository<SpaceRoleEntity, UUID> entityRepository() {
        return repository;
    }
    @Override
    public Function<SpaceRoleEntity, UUID> entityGetIdFunction() {
        return SpaceRoleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SpaceRoleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getTwinClass().getDomainId() != null
                && !entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.getTwinClass().logNormal() + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(SpaceRoleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null) {
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        }

        // Check twinClassId
        if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId())) {
            entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
        }

        // Check businessAccountId
        if (entity.getBusinessAccountId() != null) {
            if (entity.getBusinessAccount() == null || !entity.getBusinessAccount().getId().equals(entity.getBusinessAccountId())) {
                entity.setBusinessAccount(businessAccountService.findEntitySafe(entity.getBusinessAccountId()));
            }
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<SpaceRoleEntity> createSpaceRole(List<SpaceRoleCreate> spaceRoles) throws ServiceException {
        if (spaceRoles == null || spaceRoles.isEmpty()) {
            return Collections.emptyList();
        }

        i18nService.createI18nAndTranslations(I18nType.SPACE_ROLE_NAME,
                spaceRoles
                        .stream().map(SpaceRoleCreate::getNameI18n)
                        .toList());

        //todo save description

        List<SpaceRoleEntity> spaceRolesToSave = new ArrayList<>();

        for (SpaceRoleCreate spaceRole : spaceRoles) {
            SpaceRoleEntity spaceRoleEntity = new SpaceRoleEntity()
                    .setNameI18NId(spaceRole.getNameI18n().getId())
                    .setKey(spaceRole.getSpaceRole().getKey())
                    .setTwinClassId(spaceRole.getSpaceRole().getTwinClassId())
                    .setBusinessAccountId(spaceRole.getSpaceRole().getBusinessAccountId());
            spaceRolesToSave.add(spaceRoleEntity);
        }

        return StreamSupport.stream(saveSafe(spaceRolesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<SpaceRoleEntity> updateSpaceRole(List<SpaceRoleUpdate> spaceRoles) throws ServiceException {
        if (spaceRoles == null || spaceRoles.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<SpaceRoleEntity> changes = new ChangesHelperMulti<>();
        List<SpaceRoleEntity> allEntities = new ArrayList<>(spaceRoles.size());

        Kit<SpaceRoleEntity, UUID> entitiesKit = findEntitiesSafe(spaceRoles.stream().map(SpaceRoleUpdate::getId).toList());

        for (SpaceRoleUpdate spaceRole : spaceRoles) {
            SpaceRoleEntity entity = entitiesKit.get(spaceRole.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(spaceRole.getNameI18n(), I18nType.SPACE_ROLE_NAME, entity,
                    SpaceRoleEntity::getNameI18NId, SpaceRoleEntity::setNameI18NId,
                    SpaceRoleEntity.Fields.nameI18NId, changesHelper);
            i18nService.updateI18nFieldForEntity(spaceRole.getDescriptionI18n(), I18nType.SPACE_ROLE_DESCRIPTION, entity,
                    SpaceRoleEntity::getDescriptionI18NId, SpaceRoleEntity::setDescriptionI18NId,
                    SpaceRoleEntity.Fields.descriptionI18NId, changesHelper);
            updateEntityFieldByValue(spaceRole.getSpaceRole().getKey(), entity, SpaceRoleEntity::getKey, SpaceRoleEntity::setKey, SpaceRoleEntity.Fields.key, changesHelper);
            updateEntityFieldByValue(spaceRole.getSpaceRole().getTwinClassId(), entity, SpaceRoleEntity::getTwinClassId, SpaceRoleEntity::setTwinClassId, SpaceRoleEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByValue(spaceRole.getSpaceRole().getBusinessAccountId(), entity, SpaceRoleEntity::getBusinessAccountId, SpaceRoleEntity::setBusinessAccountId, SpaceRoleEntity.Fields.businessAccountId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}

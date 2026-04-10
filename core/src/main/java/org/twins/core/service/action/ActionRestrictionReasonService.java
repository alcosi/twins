package org.twins.core.service.action;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.action.ActionRestrictionReasonRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.action.ActionRestrictionReasonCreate;
import org.twins.core.domain.action.ActionRestrictionReasonUpdate;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.enums.i18n.I18nType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ActionRestrictionReasonService extends EntitySecureFindServiceImpl<ActionRestrictionReasonEntity> {
    private final ActionRestrictionReasonRepository repository;
    private final AuthService authService;
    private final I18nService i18nService;

    @Override
    public CrudRepository<ActionRestrictionReasonEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<ActionRestrictionReasonEntity, UUID> entityGetIdFunction() {
        return ActionRestrictionReasonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ActionRestrictionReasonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(apiUser.getDomain().getId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(ActionRestrictionReasonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ActionRestrictionReasonEntity> createActionRestrictionReason(Collection<ActionRestrictionReasonCreate> actionRestrictionReasons) throws ServiceException {
        if (actionRestrictionReasons == null || actionRestrictionReasons.isEmpty()) {
            return Collections.emptyList();
        }

        List<ActionRestrictionReasonEntity> entitiesToSave = new ArrayList<>();

        for (ActionRestrictionReasonCreate actionRestrictionReason : actionRestrictionReasons) {
            ActionRestrictionReasonEntity entity = new ActionRestrictionReasonEntity()
                    .setDomainId(authService.getApiUser().getDomainId())
                    .setType(actionRestrictionReason.getEntity().getType())
                    .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.ACTION_RESTRICTION_REASON_DESCRIPTION, actionRestrictionReason.getDescriptionI18n()).getId());

            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<ActionRestrictionReasonEntity> updateActionRestrictionReason(Collection<ActionRestrictionReasonUpdate> actionRestrictionReasons) throws ServiceException {
        if (actionRestrictionReasons == null || actionRestrictionReasons.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<ActionRestrictionReasonEntity> changes = new ChangesHelperMulti<>();
        List<ActionRestrictionReasonEntity> allEntities = new ArrayList<>(actionRestrictionReasons.size());

        Kit<ActionRestrictionReasonEntity, UUID> entitiesKit = findEntitiesSafe(actionRestrictionReasons.stream()
                .map(ActionRestrictionReasonUpdate::getId)
                .toList());

        for (ActionRestrictionReasonUpdate actionRestrictionReason : actionRestrictionReasons) {
            ActionRestrictionReasonEntity entity = entitiesKit.get(actionRestrictionReason.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();

            i18nService.updateI18nFieldForEntity(actionRestrictionReason.getDescriptionI18n(), I18nType.ACTION_RESTRICTION_REASON_DESCRIPTION, entity,
                    ActionRestrictionReasonEntity::getDescriptionI18nId, ActionRestrictionReasonEntity::setDescriptionI18nId,
                    ActionRestrictionReasonEntity.Fields.descriptionI18nId, changesHelper);
            updateEntityFieldByValue(actionRestrictionReason.getEntity().getType(), entity,
                    ActionRestrictionReasonEntity::getType, ActionRestrictionReasonEntity::setType,
                    ActionRestrictionReasonEntity.Fields.type, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }
}

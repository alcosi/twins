package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleRepository;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.enum_.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.permission.PermissionService;

import java.util.*;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinActionService {
    final TwinActionPermissionRepository twinActionPermissionRepository;
    final TwinActionValidatorRuleRepository twinActionValidatorRuleRepository;
    private final EntitySmartService entitySmartService;
    final TwinRepository twinRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;

    public void loadActions(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getActions() != null)
            return;
        loadClassProtectedActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getActionsProtectedByPermission().isEmpty() && twinEntity.getTwinClass().getActionsProtectedByValidatorRules().isEmpty()) {
            twinEntity.setActions(EnumSet.allOf(TwinAction.class));
            return;
        }
        twinEntity.setActions(new HashSet<>());
        for (TwinAction twinAction : TwinAction.values()) {
            TwinActionPermissionEntity twinActionProtectedByPermission = twinEntity.getTwinClass().getActionsProtectedByPermission().get(twinAction);
            if (twinActionProtectedByPermission != null) {
                if (!permissionService.hasPermission(twinEntity, twinActionProtectedByPermission.getPermissionId())) {
                    continue;
                }
            }
            if (CollectionUtils.isEmpty(twinEntity.getTwinClass().getActionsProtectedByValidatorRules().getGrouped(twinAction))) {
                twinEntity.getActions().add(twinAction); // current action is permitted
                continue;
            }
            for (TwinActionValidatorRuleEntity twinActionValidatorRuleEntity : twinEntity.getTwinClass().getActionsProtectedByValidatorRules().getGrouped(twinAction)) {
                boolean isValid = true;
                List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(twinActionValidatorRuleEntity.getTwinValidators());
                sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
                for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                    if (!twinValidatorEntity.isActive()) {
                        log.info(twinValidatorEntity.logShort() + " from " + twinActionValidatorRuleEntity.logShort() + " is inactive");
                        continue;
                    }
                    TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                    TwinValidator.ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                    if (!validationResult.isValid()) {
                        log.error(validationResult.getMessage());
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    twinEntity.getActions().add(twinAction);
                    break;
                }
            }
        }
    }

    public void loadClassProtectedActions(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getActionsProtectedByPermission() == null)
            twinClassEntity.setActionsProtectedByPermission(new Kit<>(
                    twinActionPermissionRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinActionPermissionEntity::getTwinAction));
        if (twinClassEntity.getActionsProtectedByValidatorRules() == null)
            twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(
                    twinActionValidatorRuleRepository.findByTwinClassIdOrderByOrder(twinClassEntity.getId()),
                    TwinActionValidatorRuleEntity::getId,
                    TwinActionValidatorRuleEntity::getTwinAction));
    }

    public void loadClassProtectedActions(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoadByPermissions = new HashMap<>();
        Map<UUID, TwinClassEntity> needLoadByValidators = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getActionsProtectedByPermission() == null)
                needLoadByPermissions.put(twinClassEntity.getId(), twinClassEntity);
            if (twinClassEntity.getActionsProtectedByValidatorRules() == null)
                needLoadByValidators.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (!needLoadByPermissions.isEmpty()) {
            List<TwinActionPermissionEntity> twinClassActionPermissionEntities = twinActionPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            KitGrouped<TwinActionPermissionEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionPermissionEntities, TwinActionPermissionEntity::getId, TwinActionPermissionEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                twinClassEntity.setActionsProtectedByPermission(new Kit<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionPermissionEntity::getTwinAction));
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinActionValidatorRuleEntity> twinClassActionValidatorEntities = twinActionValidatorRuleRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            KitGrouped<TwinActionValidatorRuleEntity, UUID, UUID> actionGroupedByClass = new KitGrouped<>(twinClassActionValidatorEntities, TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(actionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
            }
        }
    }

    /**
     * This method loads available actions for each `TwinEntity` object from the provided `twinEntityList`.
     * For each entity, it checks for permission-based and validator-based restrictions on actions.
     * If no restrictions exist for an entity, it is assigned all possible actions.
     * Otherwise, only actions that are not restricted by either permissions or validators are assigned.
     * The processing is grouped by the entity's class (`TwinClassEntity`).
     *
     * @param twinEntityList collection of `TwinEntity` objects for which actions need to be loaded.
     * @throws ServiceException if an error occurs during permission or validation loading.
     */
    public void loadActions(Collection<TwinEntity> twinEntityList) throws ServiceException {
        // List of entities that need action loading (those with null actions)
        List<TwinEntity> needLoad = new ArrayList<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getActions() == null)
                needLoad.add(twinEntity);
        // If there are no entities requiring action loading, exit the method
        if (needLoad.isEmpty())
            return;
        // Group TwinEntity objects by class, so permissions and validators can be processed by class
        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> groupedByClass = new KitGroupedObj<>(needLoad, TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        // Load class-protected actions for all involved classes
        loadClassProtectedActions(groupedByClass.getGroupingObjectMap().values());
        // Maps for storing forbidden actions by permissions and validators
        Map<UUID, Set<TwinAction>> twinsActionsForbiddenByPermissions = new HashMap<>();
        Map<UUID, Set<TwinAction>> twinsActionsForbiddenByValidators = new HashMap<>();

        for (Map.Entry<UUID, List<TwinEntity>> entry : groupedByClass.getGroupedMap().entrySet()) { // Loop through entities grouped by class
            List<TwinEntity> twinsNeedsValidatorCheck = new ArrayList<>(); // List of entities needing validator checks
            TwinClassEntity twinClassEntity = groupedByClass.getGroupingObject(entry.getKey());
            // Check each possible action (TwinAction) for permission and validator protection
            for (TwinAction twinAction : TwinAction.values()) {
                // Check if the action is protected by permissions
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByPermission())) {
                    TwinActionPermissionEntity classActionPermissionEntity = twinClassEntity.getActionsProtectedByPermission().get(twinAction);
                    if (classActionPermissionEntity != null) {
                        // Convert entities into permission check keys
                        Map<PermissionService.PermissionDetectKey, List<TwinEntity>> permissionDetectKeys = permissionService.convertToDetectKeys(entry.getValue());
                        // Loop through permission check keys and verify permission
                        for (Map.Entry<PermissionService.PermissionDetectKey, List<TwinEntity>> samePermissionGroupEntry : permissionDetectKeys.entrySet()) {
                            // If the permission is denied for an action, mark the action as forbidden for those entities
                            if (!permissionService.hasPermission(samePermissionGroupEntry.getKey(), classActionPermissionEntity.getPermissionId())) {
                                for (TwinEntity twinEntity : samePermissionGroupEntry.getValue()) {
                                    twinsActionsForbiddenByPermissions.computeIfAbsent(twinEntity.getId(), k -> new HashSet<>());
                                    twinsActionsForbiddenByPermissions.get(twinEntity.getId()).add(twinAction);
                                }
                            } else {
                                twinsNeedsValidatorCheck.addAll(samePermissionGroupEntry.getValue()); // If permission is granted, add to validator check list
                            }
                        }
                    }
                }
                // Check if the action is protected by validators
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidatorRules())) {
                    for (TwinActionValidatorRuleEntity actionValidatorRuleEntity : twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(twinAction)) {
                        if (!actionValidatorRuleEntity.isActive()) {
                            log.info(actionValidatorRuleEntity.logShort() + " is inactive");
                            continue;
                        }
                        // Map for checked and valid twin for current action <twin.id:uuid, valid: boolean>
                        Map<UUID, Boolean> twinByTwinValidatorsIsValid = new HashMap<>();
                        // Check each validator for the action
                        List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(actionValidatorRuleEntity.getTwinValidators());
                        sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
                        for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                            if (!twinValidatorEntity.isActive()) {
                                log.info(twinValidatorEntity.logShort() + " from " + actionValidatorRuleEntity.logShort() + " is inactive");
                                continue;
                            }
                            // Retrieve the validator and check its validity for the entities
                            TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
                            TwinValidator.CollectionValidationResult collectionValidationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinsNeedsValidatorCheck, twinValidatorEntity.isInvert());
                            // Process the validation result for each entity
                            for (TwinEntity twinEntity : twinsNeedsValidatorCheck) {
                                TwinValidator.ValidationResult validationResult = collectionValidationResult.getTwinsResults().get(twinEntity.getId());
                                if (validationResult == null) {
                                    log.warn(twinValidatorEntity.logShort() + " from " + actionValidatorRuleEntity.logShort() + " validation result should not be null");
                                    continue;
                                }
                                // compute map twin.id - valid \ invalid
                                twinByTwinValidatorsIsValid.computeIfPresent(twinEntity.getId(), (k, v) -> v && validationResult.isValid());
                                twinByTwinValidatorsIsValid.putIfAbsent(twinEntity.getId(), validationResult.isValid());
                            }
                        }
                        // Check which entities passed the validator checks and update forbidden actions
                        List<TwinEntity> nextLoopTwins = new ArrayList<>();
                        for (TwinEntity twinEntity : twinsNeedsValidatorCheck) {
                            twinsActionsForbiddenByValidators.computeIfAbsent(twinEntity.getId(), k -> new HashSet<>());
                            if (!twinByTwinValidatorsIsValid.get(twinEntity.getId())) {
                                nextLoopTwins.add(twinEntity); // If validation failed, add to next loop
                                twinsActionsForbiddenByValidators.get(twinEntity.getId()).add(twinAction); // If validation not passed, add forbidden action
                            } else {
                                twinsActionsForbiddenByValidators.get(twinEntity.getId()).remove(twinAction); // If validation passed, remove forbidden action
                            }
                        }
                        twinsNeedsValidatorCheck = nextLoopTwins; // Update list for next validator check
                    }
                }
            }
        }
        // Set allowed actions for each TwinEntity based on permissions and validator results
        for (TwinEntity twinEntity : needLoad) {
            twinEntity.setActions(new HashSet<>());
            Set<TwinAction> forbiddenPermissionActions = twinsActionsForbiddenByPermissions.get(twinEntity.getId());
            Set<TwinAction> forbiddenValidatorActions = twinsActionsForbiddenByValidators.get(twinEntity.getId());

            // If no forbidden actions, add all possible actions
            if (forbiddenPermissionActions == null && forbiddenValidatorActions == null) {
                twinEntity.getActions().addAll(EnumSet.allOf(TwinAction.class));
            } else {
                // Add only those actions that are not forbidden by either permissions or validators
                for (TwinAction action : EnumSet.allOf(TwinAction.class)) {
                    if ((forbiddenValidatorActions == null || !forbiddenValidatorActions.contains(action)) &&
                            (forbiddenPermissionActions == null || !forbiddenPermissionActions.contains(action))) {
                        twinEntity.getActions().add(action);
                    }
                }
            }
        }
    }

    public void checkAllowed(UUID twinId, TwinAction action) throws ServiceException {
        checkAllowed(entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows), action);
    }

    public void checkAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        if (!isAllowed(twinEntity, action))
            throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE, "The action[" + action.name() + "] not available for " + twinEntity.logNormal());
    }

    public boolean isAllowed(TwinEntity twinEntity, TwinAction action) throws ServiceException {
        loadActions(twinEntity);
        return twinEntity.getActions().contains(action);
    }
}

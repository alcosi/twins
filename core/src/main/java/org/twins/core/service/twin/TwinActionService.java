package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
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
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.action.TwinActionPermissionRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleRepository;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.*;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinActionService {
    final TwinActionPermissionRepository twinActionPermissionRepository;
    final TwinActionValidatorRuleRepository twinActionValidatorRuleRepository;
    private final EntitySmartService entitySmartService;
    final TwinRepository twinRepository;
    @Lazy
    final TwinValidatorService twinValidatorService;
    @Lazy
    final TwinValidatorSetService twinValidatorSetService;
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
                boolean isValid = twinValidatorSetService.isValid(twinEntity, twinActionValidatorRuleEntity);
                if (isValid) {
                    twinEntity.getActions().add(twinAction);
                    break;
                }
            }
        }
    }

    public void loadClassProtectedActions(TwinClassEntity twinClassEntity) throws ServiceException {
        loadClassProtectedActions(Collections.singletonList(twinClassEntity));
    }

    public void loadClassProtectedActions(Collection<TwinClassEntity> srcCollection) throws ServiceException {
        List<TwinClassEntity> needLoadByPermissions = new ArrayList<>();
        List<TwinClassEntity> needLoadByValidators = new ArrayList<>();
        Set<UUID> needLoadByPermissionsClassIds = new HashSet<>();
        Set<UUID> needLoadBysValidatorsClassIds = new HashSet<>();
        for (TwinClassEntity twinClassEntity : srcCollection) {
            if (twinClassEntity.getActionsProtectedByPermission() == null) {
                needLoadByPermissionsClassIds.addAll(twinClassEntity.getExtendedClassIdSet()); // rules are inherited
                needLoadByPermissions.add(twinClassEntity);
            }
            if (twinClassEntity.getActionsProtectedByValidatorRules() == null) {
                needLoadBysValidatorsClassIds.addAll(twinClassEntity.getExtendedClassIdSet()); // rules are inherited
                needLoadByValidators.add(twinClassEntity);
            }
        }
        needLoadByPermissionsClassIds.remove(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR);
        if (!needLoadByPermissions.isEmpty()) {
            List<TwinActionPermissionEntity> twinClassActionPermissionEntities = twinActionPermissionRepository.findByTwinClassIdIn(needLoadByPermissionsClassIds);
            Map<TwinAction, Map<UUID, TwinActionPermissionEntity>> groupedByActionThenByClass = new HashMap<>();
            for (var twinActionPermissionEntity : twinClassActionPermissionEntities) {
                groupedByActionThenByClass.computeIfAbsent(twinActionPermissionEntity.getTwinAction(), k -> new HashMap<>()).put(twinActionPermissionEntity.getTwinClassId(), twinActionPermissionEntity);
            }
            for (TwinClassEntity twinClassEntity : needLoadByPermissions) {
                twinClassEntity.setActionsProtectedByPermission(new Kit<>(TwinActionPermissionEntity::getTwinAction));
                for (var action : TwinAction.values()) {
                    if (twinClassEntity.getActionsProtectedByPermission().get(action) != null)
                        continue; // protectedByPermission is already detected
                    if (groupedByActionThenByClass.get(action) == null) {
                        continue; // protectedByPermission is not configured to any class
                    }
                    for (var extendsClassId : twinClassEntity.getExtendedClassIdSet()) { // set order is important
                        var actionPermission = groupedByActionThenByClass.get(action).get(extendsClassId);
                        if (actionPermission != null) {
                            twinClassEntity.getActionsProtectedByPermission().add(actionPermission);
                            break; // no need to go deeper
                        }
                    }
                }
            }
        }
        if (!needLoadByValidators.isEmpty()) {
            List<TwinActionValidatorRuleEntity> twinClassActionValidatorEntities = twinActionValidatorRuleRepository.findByTwinClassIdIn(needLoadBysValidatorsClassIds);
            twinValidatorService.loadValidators(twinClassActionValidatorEntities);
            Map<TwinAction, Map<UUID, TwinActionValidatorRuleEntity>> groupedByActionThenByClass = new HashMap<>();
            for (var twinActionValidatorRuleEntity : twinClassActionValidatorEntities) {
                groupedByActionThenByClass.computeIfAbsent(twinActionValidatorRuleEntity.getTwinAction(), k -> new HashMap<>()).put(twinActionValidatorRuleEntity.getTwinClassId(), twinActionValidatorRuleEntity);
            }
            for (TwinClassEntity twinClassEntity : needLoadByValidators) {
                twinClassEntity.setActionsProtectedByValidatorRules(new KitGrouped<>(TwinActionValidatorRuleEntity::getId, TwinActionValidatorRuleEntity::getTwinAction));
                for (var action : TwinAction.values()) {
                    if (CollectionUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(action)))
                        continue; // protectedByValidator is already detected
                    if (groupedByActionThenByClass.get(action) == null) {
                        continue; // protectedByValidator is not configured to any class
                    }
                    for (var extendsClassId : twinClassEntity.getExtendedClassIdSet()) { // set order is important
                        var actionPermission = groupedByActionThenByClass.get(action).get(extendsClassId);
                        if (actionPermission != null) {
                            twinClassEntity.getActionsProtectedByValidatorRules().add(actionPermission);
                            break; // no need to go deeper
                        }
                    }
                }
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

        for (Map.Entry<UUID, List<TwinEntity>> entry : groupedByClass.getGroupedMap().entrySet()) {
            List<TwinEntity> classTwins = entry.getValue();
            TwinClassEntity twinClassEntity = groupedByClass.getGroupingObject(entry.getKey());

            // For each action, check both permissions and validators
            for (TwinAction twinAction : TwinAction.values()) {
                // List of twins that need validator check for this specific action
                List<TwinEntity> twinsForValidatorCheck = new ArrayList<>();

                // Check if this action is protected by permission
                TwinActionPermissionEntity classActionPermissionEntity = null;
                if (KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByPermission())) {
                    classActionPermissionEntity = twinClassEntity.getActionsProtectedByPermission().get(twinAction);
                }

                if (classActionPermissionEntity != null) {
                    // Action is protected by permission - check permissions
                    Map<PermissionService.PermissionDetectKey, List<TwinEntity>> permissionDetectKeys = permissionService.convertToDetectKeys(classTwins);

                    for (Map.Entry<PermissionService.PermissionDetectKey, List<TwinEntity>> samePermissionGroupEntry :
                            permissionDetectKeys.entrySet()) {

                        if (!permissionService.hasPermission(samePermissionGroupEntry.getKey(), classActionPermissionEntity.getPermissionId())) {
                            // Permission denied - mark action as forbidden
                            for (TwinEntity twinEntity : samePermissionGroupEntry.getValue()) {
                                twinsActionsForbiddenByPermissions
                                        .computeIfAbsent(twinEntity.getId(), k -> new HashSet<>())
                                        .add(twinAction);
                            }
                        } else {
                            // Permission granted - add to validator check list
                            twinsForValidatorCheck.addAll(samePermissionGroupEntry.getValue());
                        }
                    }
                } else {
                    // Action is NOT protected by permission - all twins go to validator check
                    twinsForValidatorCheck.addAll(classTwins);
                }

                // Check if action is protected by validators (if there are twins to check)
                if (!twinsForValidatorCheck.isEmpty() && KitUtils.isNotEmpty(twinClassEntity.getActionsProtectedByValidatorRules())) {
                    List<TwinActionValidatorRuleEntity> validatorRules = twinClassEntity.getActionsProtectedByValidatorRules().getGrouped(twinAction);

                    if (validatorRules != null && !validatorRules.isEmpty()) {
                        // Process each validator rule for this action
                        for (TwinActionValidatorRuleEntity actionValidatorRuleEntity : validatorRules) {
                            if (!actionValidatorRuleEntity.isActive()) {
                                log.info(actionValidatorRuleEntity.logShort() + " is inactive");
                                continue;
                            }

                            // List of twins that passed all previous validators and need to be checked
                            List<TwinEntity> twinsToCheck = new ArrayList<>(twinsForValidatorCheck);
                            // Clear the list for this rule's results
                            twinsForValidatorCheck = new ArrayList<>();

                            // Map to track validation results for each twin
                            Map<UUID, Boolean> twinValidationResults = new HashMap<>();

                            // Load and sort validators
                            twinValidatorService.loadValidators(actionValidatorRuleEntity);
                            List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(actionValidatorRuleEntity.getTwinValidatorKit().getList());
                            sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));

                            // Apply each validator
                            for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                                if (!twinValidatorEntity.isActive()) {
                                    log.info(twinValidatorEntity.logShort() + " from " + actionValidatorRuleEntity.logShort() + " is inactive");
                                    continue;
                                }

                                TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
                                TwinValidator.CollectionValidationResult collectionValidationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinsToCheck, twinValidatorEntity.isInvert());

                                // Process validation results
                                for (TwinEntity twinEntity : twinsToCheck) {
                                    ValidationResult validationResult = collectionValidationResult.getTwinsResults().get(twinEntity.getId());
                                    if (validationResult == null) {
                                        log.warn(twinValidatorEntity.logShort() + " from " + actionValidatorRuleEntity.logShort() + " validation result should not be null");
                                        continue;
                                    }
                                    // Combine results (AND logic - all validators must pass)
                                    boolean isValid = twinValidationResults.getOrDefault(twinEntity.getId(), true) && validationResult.isValid();
                                    twinValidationResults.put(twinEntity.getId(), isValid);
                                }
                            }

                            // Separate twins that passed validation from those that failed
                            for (TwinEntity twinEntity : twinsToCheck) {
                                boolean isValid = twinValidationResults.getOrDefault(twinEntity.getId(), false);
                                if (isValid) {
                                    // Twin passed this validator rule - keep for next rule
                                    twinsForValidatorCheck.add(twinEntity);
                                } else {
                                    // Twin failed validation - mark action as forbidden
                                    twinsActionsForbiddenByValidators
                                            .computeIfAbsent(twinEntity.getId(), k -> new HashSet<>())
                                            .add(twinAction);
                                }
                            }
                        }
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
                    if ((forbiddenValidatorActions == null || !forbiddenValidatorActions.contains(action)) && (forbiddenPermissionActions == null || !forbiddenPermissionActions.contains(action))) {
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

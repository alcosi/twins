package org.twins.core.service.attachment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.attachment.TwinAttachmentActionAlienPermissionEntity;
import org.twins.core.dao.attachment.TwinAttachmentActionAlienPermissionRepository;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.*;
import org.twins.core.enums.attachment.TwinAttachmentAction;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.*;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class AttachmentActionService {
    private final AuthService authService;
    private final TwinAttachmentActionAlienPermissionRepository twinAttachmentActionAlienPermissionRepository;
    private final TwinAttachmentActionAlienValidatorRuleRepository twinAttachmentActionAlienValidatorRuleRepository;
    private final TwinAttachmentActionSelfValidatorRuleRepository twinAttachmentActionSelfValidatorRuleRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    private final TwinValidatorService twinValidatorService;

    public void loadAttachmentActions(TwinAttachmentEntity twinAttachment) throws ServiceException {
        if (twinAttachment.getAttachmentActions() != null)
            return;
        if (twinAttachment.getCreatedByUserId().equals(authService.getApiUser().getUserId()))
            loadAttachmentSelfActions(twinAttachment);
        else
            loadAttachmentAlienActions(twinAttachment);
    }

    private void loadAttachmentAlienActions(TwinAttachmentEntity twinAttachment) throws ServiceException {
        TwinEntity twinEntity = twinAttachment.getTwin();
        loadClassAttachmentActionsAlienProtected(twinEntity.getTwinClass());
        if (KitUtils.isEmpty(twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByPermission()) && KitUtils.isEmpty(twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByValidatorRules())) {
            twinAttachment.setAttachmentActions(Collections.EMPTY_SET);
            return;
        }
        twinAttachment.setAttachmentActions(new HashSet<>());
        for (TwinAttachmentAction twinAttachmentAction : TwinAttachmentAction.values()) {
            TwinAttachmentActionAlienPermissionEntity twinAttachmentActionAlienPermission = twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByPermission().get(twinAttachmentAction);
            if (twinAttachmentActionAlienPermission != null) {
                if (permissionService.hasPermission(twinEntity, twinAttachmentActionAlienPermission.getPermissionId())) {
                    twinAttachment.getAttachmentActions().add(twinAttachmentAction);
                    continue;
                }
            }
            if (KitUtils.isEmpty(twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByValidatorRules()))
                continue;
            boolean someRuleIsValid = false;
            for (TwinAttachmentActionAlienValidatorRuleEntity twinAttachmentActionAlienValidatorRule : twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByValidatorRules().getGrouped(twinAttachmentAction)) {
                if (!twinAttachmentActionAlienValidatorRule.isActive()) {
                    log.info("{} will not be used, since it is inactive", twinAttachmentActionAlienValidatorRule.easyLog(EasyLoggable.Level.NORMAL));
                    continue;
                }
                twinValidatorService.loadValidators(twinEntity.getTwinClass().getAttachmentAlienActionsProtectedByValidatorRules());
                List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(twinAttachmentActionAlienValidatorRule.getTwinValidatorKit().getList());
                sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
                boolean allRuleValidatorsAreValid = true;
                for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                    if (!twinValidatorEntity.isActive()) {
                        log.info("{} from {} will not be used, since it is inactive. ", twinValidatorEntity.easyLog(EasyLoggable.Level.NORMAL), twinAttachmentActionAlienValidatorRule.easyLog(EasyLoggable.Level.NORMAL));
                        continue;
                    }
                    TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
                    ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                    if (!validationResult.isValid()) {
                        log.error(validationResult.getMessage());
                        allRuleValidatorsAreValid = false;
                        break;
                    }
                }
                if (allRuleValidatorsAreValid) {
                    someRuleIsValid = true;
                    break;
                }
            }
            if (someRuleIsValid)
                twinAttachment.getAttachmentActions().add(twinAttachmentAction);
        }
    }

    private void loadAttachmentSelfActions(TwinAttachmentEntity twinAttachment) throws ServiceException {
        TwinEntity twin = twinAttachment.getTwin();
        loadClassAttachmentActionsSelfRestrict(twin.getTwinClass());
        twinAttachment.setAttachmentActions(EnumSet.allOf(TwinAttachmentAction.class));
        if (KitUtils.isEmpty(twin.getTwinClass().getAttachmentSelfActionsRestriction()))
            return;
        for (TwinAttachmentAction action : TwinAttachmentAction.values()) {
            boolean isRestricted = false;
            for (TwinAttachmentActionSelfValidatorRuleEntity twinAttachmentActionSelfValidatorRuleEntity : twin.getTwinClass().getAttachmentSelfActionsRestriction().getGrouped(action)) {
                if (!twinAttachmentActionSelfValidatorRuleEntity.isActive()) {
                    log.info("{} will not be used, since it is inactive", twinAttachmentActionSelfValidatorRuleEntity.logNormal());
                    continue;
                }
                List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(twinAttachmentActionSelfValidatorRuleEntity.getTwinValidators());
                sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
                for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                    if (!twinValidatorEntity.isActive()) {
                        log.info("{} from {} will not be used, since it is inactive. ", twinValidatorEntity.logNormal(), twinAttachmentActionSelfValidatorRuleEntity.logNormal());
                        continue;
                    }
                    TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
                    ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twin, twinValidatorEntity.isInvert());
                    if (validationResult.isValid()) {
                        log.error(validationResult.getMessage());
                        isRestricted = true;
                        break;
                    }
                }
                if (isRestricted) break;
            }
            if (isRestricted)
                twinAttachment.getAttachmentActions().remove(action);
        }
    }

    private void loadClassAttachmentActionsAlienProtected(TwinClassEntity twinClass) {
        if (twinClass.getAttachmentAlienActionsProtectedByPermission() == null)
            twinClass.setAttachmentAlienActionsProtectedByPermission(new Kit<>(
                    twinAttachmentActionAlienPermissionRepository.findByTwinClassId(twinClass.getId()),
                    TwinAttachmentActionAlienPermissionEntity::getTwinAttachmentAction));
        if (twinClass.getAttachmentAlienActionsProtectedByValidatorRules() == null)
            twinClass.setAttachmentAlienActionsProtectedByValidatorRules(new KitGrouped<>(
                    twinAttachmentActionAlienValidatorRuleRepository.findByTwinClassIdOrderByOrder(twinClass.getId()),
                    TwinAttachmentActionAlienValidatorRuleEntity::getId,
                    TwinAttachmentActionAlienValidatorRuleEntity::getTwinAttachmentAction
            ));
    }

    public void loadClassAttachmentActionsAlienProtected(Collection<TwinClassEntity> twinClassCollection) throws ServiceException {
        Map<UUID, TwinClassEntity> needLoadByPermissions = new HashMap<>();
        Map<UUID, TwinClassEntity> needLoadByValidators = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getAttachmentAlienActionsProtectedByPermission() == null)
                needLoadByPermissions.put(twinClassEntity.getId(), twinClassEntity);
            if (twinClassEntity.getAttachmentAlienActionsProtectedByValidatorRules() == null)
                needLoadByValidators.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isNotEmpty(needLoadByPermissions)) {
            List<TwinAttachmentActionAlienPermissionEntity> twinClassAttachmentActionAlienPermissionEntities = twinAttachmentActionAlienPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            KitGrouped<TwinAttachmentActionAlienPermissionEntity, UUID, UUID> attachmentActionGroupedByClass =
                    new KitGrouped<>(twinClassAttachmentActionAlienPermissionEntities,
                            TwinAttachmentActionAlienPermissionEntity::getId,
                            TwinAttachmentActionAlienPermissionEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                twinClassEntity.setAttachmentAlienActionsProtectedByPermission(
                        new Kit<>(attachmentActionGroupedByClass.getGrouped(twinClassEntity.getId()),
                                TwinAttachmentActionAlienPermissionEntity::getTwinAttachmentAction));
            }
        }

        if (MapUtils.isNotEmpty(needLoadByValidators)) {
            List<TwinAttachmentActionAlienValidatorRuleEntity> twinClassAttachmentActionValidatorEntities =
                    twinAttachmentActionAlienValidatorRuleRepository.findByTwinClassIdIn(needLoadByValidators.keySet());
            twinValidatorService.loadValidators(twinClassAttachmentActionValidatorEntities);
            KitGrouped<TwinAttachmentActionAlienValidatorRuleEntity, UUID, UUID> attachmentActionGroupedByClass =
                    new KitGrouped<>(twinClassAttachmentActionValidatorEntities,
                            TwinAttachmentActionAlienValidatorRuleEntity::getId,
                            TwinAttachmentActionAlienValidatorRuleEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                twinClassEntity.setAttachmentAlienActionsProtectedByValidatorRules(
                        new KitGrouped<>(attachmentActionGroupedByClass.getGrouped(twinClassEntity.getId()),
                                TwinAttachmentActionAlienValidatorRuleEntity::getId,
                                TwinAttachmentActionAlienValidatorRuleEntity::getTwinAttachmentAction));
            }
        }
    }

    public void loadClassAttachmentActionsSelfRestrict(TwinClassEntity twinClass) {
        loadClassAttachmentActionsSelfRestrict(Collections.singletonList(twinClass));
    }

    public void loadClassAttachmentActionsSelfRestrict(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoad = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getAttachmentSelfActionsRestriction() == null)
                needLoad.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        List<TwinAttachmentActionSelfValidatorRuleEntity> twinClassAttachmentActionSelfEntities =
                twinAttachmentActionSelfValidatorRuleRepository.findByTwinClassIdIn(needLoad.keySet());
        KitGrouped<TwinAttachmentActionSelfValidatorRuleEntity, UUID, UUID> attachmentActionGroupedByClass =
                new KitGrouped<>(twinClassAttachmentActionSelfEntities,
                        TwinAttachmentActionSelfValidatorRuleEntity::getId,
                        TwinAttachmentActionSelfValidatorRuleEntity::getTwinClassId);
        for (TwinClassEntity twinClassEntity : needLoad.values()) {
            twinClassEntity.setAttachmentSelfActionsRestriction(
                    new KitGrouped<>(attachmentActionGroupedByClass.getGrouped(twinClassEntity.getId()),
                            TwinAttachmentActionSelfValidatorRuleEntity::getId,
                            TwinAttachmentActionSelfValidatorRuleEntity::getRestrictTwinAttachmentAction));
        }
    }

    public void loadAttachmentActions(Collection<TwinAttachmentEntity> twinAttachments) throws ServiceException {
        List<TwinAttachmentEntity> needLoad = new ArrayList<>();
        Set<TwinClassEntity> needLoadAttachmentActionsAlienProtected = new HashSet<>();
        Set<TwinClassEntity> needLoadAttachmentActionsSelfRestrict = new HashSet<>();
        UUID currentUserId = authService.getApiUser().getUserId();
        for (TwinAttachmentEntity twinAttachment : twinAttachments) {
            if (twinAttachment.getAttachmentActions() != null)
                continue;
            needLoad.add(twinAttachment);

            TwinClassEntity twinClassEntity = twinAttachment.getTwin().getTwinClass();
            if (twinAttachment.getCreatedByUserId().equals(currentUserId) && twinClassEntity.getAttachmentSelfActionsRestriction() == null) {
                needLoadAttachmentActionsSelfRestrict.add(twinClassEntity);
            } else if (twinClassEntity.getAttachmentAlienActionsProtectedByValidatorRules() == null || twinClassEntity.getAttachmentAlienActionsProtectedByPermission() == null) {
                needLoadAttachmentActionsAlienProtected.add(twinClassEntity);
            }
        }
        if (needLoad.isEmpty())
            return;
        if (CollectionUtils.isNotEmpty(needLoadAttachmentActionsAlienProtected))
            loadClassAttachmentActionsAlienProtected(needLoadAttachmentActionsAlienProtected);
        if (CollectionUtils.isNotEmpty(needLoadAttachmentActionsSelfRestrict))
            loadClassAttachmentActionsSelfRestrict(needLoadAttachmentActionsSelfRestrict);
        for (TwinAttachmentEntity twinAttachment : needLoad)
            loadAttachmentActions(twinAttachment);  //now it's N+1 safe to do it loop because TwinClassEntities are already loaded with all necessary data
    }

    public void checkAllowed(TwinAttachmentEntity twinAttachmentEntity, TwinAttachmentAction action) throws ServiceException {
        if (!isAllowed(twinAttachmentEntity, action))
            throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE,
                    "The action[" + action.name() + "] not available for attachment[" + twinAttachmentEntity.getId() + "] on " + twinAttachmentEntity.getTwin().logNormal());
    }

    public boolean isAllowed(TwinAttachmentEntity twinAttachmentEntity, TwinAttachmentAction action) throws ServiceException {
        loadAttachmentActions(twinAttachmentEntity);
        return twinAttachmentEntity.getAttachmentActions().contains(action);
    }
}

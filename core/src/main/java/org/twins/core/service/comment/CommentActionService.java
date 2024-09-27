package org.twins.core.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.comment.*;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.util.*;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentActionService {
    final TwinCommentActionAlienPermissionRepository twinCommentActionAlienPermissionRepository;
    final TwinCommentActionAlienValidatorRepository twinCommentActionAlienValidatorRepository;
    private final TwinCommentActionSelfRepository twinCommentActionSelfRepository;
    final TwinRepository twinRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;

    private final AuthService authService;

    public void loadCommentActions(TwinCommentEntity twinComment) throws ServiceException {
        if (twinComment.getCommentActions() != null)
            return;
        if (twinComment.getCreatedByUserId().equals(authService.getApiUser().getUserId()))
            loadCommentSelfActions(twinComment);
        else
            loadCommentAlienActions(twinComment);
    }

    private void loadCommentAlienActions(TwinCommentEntity twinComment) throws ServiceException {
        TwinEntity twinEntity = twinComment.getTwinByTwinId();
        loadClassProtectedCommentActions(twinEntity.getTwinClass());
        if (twinEntity.getTwinClass().getCommentAlienActionsProtectedByPermission().isEmpty()) {
            twinComment.setCommentActions(EnumSet.allOf(TwinCommentAction.class));
            return;
        }
        twinComment.setCommentActions(new HashSet<>());
        for (TwinCommentAction twinCommentAction : TwinCommentAction.values()) {
            TwinCommentActionAlienPermissionEntity twinCommentActionAlienPermission = twinEntity.getTwinClass().getCommentAlienActionsProtectedByPermission().get(twinCommentAction);
            if (twinCommentActionAlienPermission != null) {
                if (!permissionService.hasPermission(twinEntity, twinCommentActionAlienPermission.getPermissionId()))
                    continue; // current comment action is forbidden
            }
            if (KitUtils.isEmpty(twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidator())) {
                twinComment.getCommentActions().add(twinCommentAction); // current comment action is permitted
                continue;
            }
            boolean isValid = true;
            for (TwinCommentActionAlienValidatorEntity twinCommentActionAlienValidator : twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidator().getGrouped(twinCommentAction)) {
                TwinValidator twinValidator = featurerService.getFeaturer(twinCommentActionAlienValidator.getTwinValidatorFeaturer(), TwinValidator.class);
                TwinValidator.ValidationResult validationResult = twinValidator.isValid(twinCommentActionAlienValidator.getTwinValidatorParams(), twinEntity, twinCommentActionAlienValidator.isInvert());
                if (!validationResult.isValid()) {
                    log.error(validationResult.getMessage());
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                twinComment.getCommentActions().add(twinCommentAction);
            }
        }
    }

    private void loadCommentSelfActions(TwinCommentEntity twinComment) {
        TwinEntity twinEntity = twinComment.getTwinByTwinId();
        loadClassRestrictSelfCommentActions(twinEntity.getTwinClass());
        twinComment.setCommentActions(EnumSet.allOf(TwinCommentAction.class));
        if (twinEntity.getTwinClass().getCommentSelfActionsRestriction().isEmpty())
            return;
        for (TwinCommentAction action: twinEntity.getTwinClass().getCommentSelfActionsRestriction().getIdSet()) {
            twinComment.getCommentActions().remove(action);
        }
    }

    public void loadClassProtectedCommentActions(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getCommentAlienActionsProtectedByPermission() == null)
            twinClassEntity.setCommentAlienActionsProtectedByPermission(new Kit<>(
                    twinCommentActionAlienPermissionRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinCommentActionAlienPermissionEntity::getTwinCommentAction));
        if (twinClassEntity.getCommentAlienActionsProtectedByValidator() == null)
            twinClassEntity.setCommentAlienActionsProtectedByValidator(new KitGrouped<>(
                    twinCommentActionAlienValidatorRepository.findByTwinClassIdOrderByOrder(twinClassEntity.getId()),
                    TwinCommentActionAlienValidatorEntity::getId,
                    TwinCommentActionAlienValidatorEntity::getTwinCommentAction));
    }

    public void loadClassRestrictSelfCommentActions(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getCommentSelfActionsRestriction() == null)
            twinClassEntity.setCommentSelfActionsRestriction(new Kit<>(
                    twinCommentActionSelfRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinCommentActionSelfEntity::getRestrictTwinCommentAction));
    }

    public void loadClassProtectedCommentActions(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoadByPermissions = new HashMap<>();
        Map<UUID, TwinClassEntity> needLoadByValidators = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getCommentAlienActionsProtectedByPermission() == null)
                needLoadByPermissions.put(twinClassEntity.getId(), twinClassEntity);
            if (twinClassEntity.getCommentAlienActionsProtectedByValidator() == null)
                needLoadByValidators.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isNotEmpty(needLoadByPermissions)) {
            List<TwinCommentActionAlienPermissionEntity> twinClassCommentActionAlienPermissionEntities = twinCommentActionAlienPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (CollectionUtils.isNotEmpty(twinClassCommentActionAlienPermissionEntities)) {
                KitGrouped<TwinCommentActionAlienPermissionEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionAlienPermissionEntities, TwinCommentActionAlienPermissionEntity::getId, TwinCommentActionAlienPermissionEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                    twinClassEntity.setCommentAlienActionsProtectedByPermission(new Kit<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionAlienPermissionEntity::getTwinCommentAction));
                }
            }
        }
        if (MapUtils.isNotEmpty(needLoadByValidators)) {
            List<TwinCommentActionAlienValidatorEntity> twinClassCommentActionValidatorEntities = twinCommentActionAlienValidatorRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            if (CollectionUtils.isNotEmpty(twinClassCommentActionValidatorEntities)) {
                KitGrouped<TwinCommentActionAlienValidatorEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionValidatorEntities, TwinCommentActionAlienValidatorEntity::getId, TwinCommentActionAlienValidatorEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                    twinClassEntity.setCommentAlienActionsProtectedByValidator(new KitGrouped<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionAlienValidatorEntity::getId, TwinCommentActionAlienValidatorEntity::getTwinCommentAction));
                }
            }
        }
    }

    public void loadClassRestrictSelfCommentActions(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoad = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getCommentSelfActionsRestriction() == null)
                needLoad.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isNotEmpty(needLoad)) {
            List<TwinCommentActionSelfEntity> twinClassCommentActionSelfEntities = twinCommentActionSelfRepository.findByTwinClassIdIn(needLoad.keySet());
            if (CollectionUtils.isNotEmpty(twinClassCommentActionSelfEntities)) {
                KitGrouped<TwinCommentActionSelfEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionSelfEntities, TwinCommentActionSelfEntity::getId, TwinCommentActionSelfEntity::getTwinClassId);
                for (TwinClassEntity twinClassEntity : needLoad.values()) {
                    twinClassEntity.setCommentSelfActionsRestriction(new Kit<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionSelfEntity::getRestrictTwinCommentAction));
                }
            }
        }
    }

    public void loadCommentActions(Collection<TwinCommentEntity> twinComments) throws ServiceException {
        //todo need implementation
    }
}

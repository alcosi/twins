package org.twins.core.service.comment;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
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
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.validator.TwinCommentActionAlienValidatorRuleEntity;
import org.twins.core.dao.validator.TwinCommentActionAlienValidatorRuleRepository;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.enums.comment.TwinCommentAction;
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
public class CommentActionService {
    final TwinCommentActionAlienPermissionRepository twinCommentActionAlienPermissionRepository;
    final TwinCommentActionAlienValidatorRuleRepository twinCommentActionAlienValidatorRuleRepository;
    private final TwinCommentActionSelfRepository twinCommentActionSelfRepository;
    @Lazy
    final PermissionService permissionService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final TwinValidatorService twinValidatorService;

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
        TwinEntity twinEntity = twinComment.getTwin();
        loadClassCommentActionsAlienProtected(twinEntity.getTwinClass());
        if (KitUtils.isEmpty(twinEntity.getTwinClass().getCommentAlienActionsProtectedByPermission()) && KitUtils.isEmpty(twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidatorRules())) {
            twinComment.setCommentActions(Collections.EMPTY_SET);
            return;
        }
        twinComment.setCommentActions(new HashSet<>());
        for (TwinCommentAction twinCommentAction : TwinCommentAction.values()) {
            TwinCommentActionAlienPermissionEntity twinCommentActionAlienPermission = twinEntity.getTwinClass().getCommentAlienActionsProtectedByPermission().get(twinCommentAction);
            if (twinCommentActionAlienPermission != null) {
                if (permissionService.hasPermission(twinEntity, twinCommentActionAlienPermission.getPermissionId())) {
                    twinComment.getCommentActions().add(twinCommentAction);
                    continue; // current comment action is permitted (we will not check validators)
                }
            }
            if (KitUtils.isEmpty(twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidatorRules())) {
                continue;
            }
            boolean isValid = true;
            for (TwinCommentActionAlienValidatorRuleEntity twinCommentActionAlienValidatorRule : twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidatorRules().getGrouped(twinCommentAction)) {
                if (!twinCommentActionAlienValidatorRule.isActive()) {
                    log.info("{} will not be used, since it is inactive.", twinCommentActionAlienValidatorRule.easyLog(EasyLoggable.Level.NORMAL));
                    continue;
                }
                twinValidatorService.loadValidators(twinEntity.getTwinClass().getCommentAlienActionsProtectedByValidatorRules());
                List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(twinCommentActionAlienValidatorRule.getTwinValidatorKit().getList());
                sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
                isValid = true;
                for(TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
                    if (!twinValidatorEntity.isActive()) {
                        log.info("{} from {} will not be used, since it is inactive.", twinValidatorEntity.easyLog(EasyLoggable.Level.NORMAL), twinCommentActionAlienValidatorRule.easyLog(EasyLoggable.Level.NORMAL));
                        continue;
                    }
                    TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
                    ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
                    if (!validationResult.isValid()) {
                        log.error(validationResult.getMessage());
                        isValid = false;
                        break;
                    }
                }
                if (isValid) break;
            }
            if (isValid)
                twinComment.getCommentActions().add(twinCommentAction);
        }
    }

    private void loadCommentSelfActions(TwinCommentEntity twinComment) {
        TwinEntity twinEntity = twinComment.getTwin();
        loadClassCommentActionsSelfRestrict(twinEntity.getTwinClass());
        twinComment.setCommentActions(EnumSet.allOf(TwinCommentAction.class));
        if (KitUtils.isEmpty(twinEntity.getTwinClass().getCommentSelfActionsRestriction()))
            return;
        for (TwinCommentAction action : twinEntity.getTwinClass().getCommentSelfActionsRestriction().getIdSet()) {
            twinComment.getCommentActions().remove(action);
        }
    }

    public void loadClassCommentActionsAlienProtected(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getCommentAlienActionsProtectedByPermission() == null)
            twinClassEntity.setCommentAlienActionsProtectedByPermission(new Kit<>(
                    twinCommentActionAlienPermissionRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinCommentActionAlienPermissionEntity::getTwinCommentAction));
        if (twinClassEntity.getCommentAlienActionsProtectedByValidatorRules() == null)
            twinClassEntity.setCommentAlienActionsProtectedByValidatorRules(new KitGrouped<>(
                    twinCommentActionAlienValidatorRuleRepository.findByTwinClassIdOrderByOrder(twinClassEntity.getId()),
                    TwinCommentActionAlienValidatorRuleEntity::getId,
                    TwinCommentActionAlienValidatorRuleEntity::getTwinCommentAction));
    }

    public void loadClassCommentActionsSelfRestrict(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getCommentSelfActionsRestriction() == null)
            twinClassEntity.setCommentSelfActionsRestriction(new Kit<>(
                    twinCommentActionSelfRepository.findByTwinClassId(twinClassEntity.getId()),
                    TwinCommentActionSelfEntity::getRestrictTwinCommentAction));
    }

    public void loadClassCommentActionsAlienProtected(Collection<TwinClassEntity> twinClassCollection) throws ServiceException {
        Map<UUID, TwinClassEntity> needLoadByPermissions = new HashMap<>();
        Map<UUID, TwinClassEntity> needLoadByValidators = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getCommentAlienActionsProtectedByPermission() == null)
                needLoadByPermissions.put(twinClassEntity.getId(), twinClassEntity);
            if (twinClassEntity.getCommentAlienActionsProtectedByValidatorRules() == null)
                needLoadByValidators.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isNotEmpty(needLoadByPermissions)) {
            List<TwinCommentActionAlienPermissionEntity> twinClassCommentActionAlienPermissionEntities = twinCommentActionAlienPermissionRepository.findByTwinClassIdIn(needLoadByPermissions.keySet());
            KitGrouped<TwinCommentActionAlienPermissionEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionAlienPermissionEntities, TwinCommentActionAlienPermissionEntity::getId, TwinCommentActionAlienPermissionEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByPermissions.values()) {
                twinClassEntity.setCommentAlienActionsProtectedByPermission(new Kit<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionAlienPermissionEntity::getTwinCommentAction));
            }
        }
        if (MapUtils.isNotEmpty(needLoadByValidators)) {
            List<TwinCommentActionAlienValidatorRuleEntity> twinClassCommentActionValidatorEntities = twinCommentActionAlienValidatorRuleRepository.findByTwinClassIdIn(needLoadByValidators.keySet());
            twinValidatorService.loadValidators(twinClassCommentActionValidatorEntities);
            KitGrouped<TwinCommentActionAlienValidatorRuleEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionValidatorEntities, TwinCommentActionAlienValidatorRuleEntity::getId, TwinCommentActionAlienValidatorRuleEntity::getTwinClassId);
            for (TwinClassEntity twinClassEntity : needLoadByValidators.values()) {
                twinClassEntity.setCommentAlienActionsProtectedByValidatorRules(new KitGrouped<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionAlienValidatorRuleEntity::getId, TwinCommentActionAlienValidatorRuleEntity::getTwinCommentAction));
            }
        }
    }

    public void loadClassCommentActionsSelfRestrict(Collection<TwinClassEntity> twinClassCollection) {
        Map<UUID, TwinClassEntity> needLoad = new HashMap<>();
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getCommentSelfActionsRestriction() == null)
                needLoad.put(twinClassEntity.getId(), twinClassEntity);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        List<TwinCommentActionSelfEntity> twinClassCommentActionSelfEntities = twinCommentActionSelfRepository.findByTwinClassIdIn(needLoad.keySet());
        KitGrouped<TwinCommentActionSelfEntity, UUID, UUID> commentActionGroupedByClass = new KitGrouped<>(twinClassCommentActionSelfEntities, TwinCommentActionSelfEntity::getId, TwinCommentActionSelfEntity::getTwinClassId);
        for (TwinClassEntity twinClassEntity : needLoad.values()) {
            twinClassEntity.setCommentSelfActionsRestriction(new Kit<>(commentActionGroupedByClass.getGrouped(twinClassEntity.getId()), TwinCommentActionSelfEntity::getRestrictTwinCommentAction));
        }
    }

    // this method can give some load speedup in case when input list of comments belong to different twins of different twin classes.
    // For example if it is called from comment search API
    public void loadCommentActions(Collection<TwinCommentEntity> twinComments) throws ServiceException {
        List<TwinCommentEntity> needLoad = new ArrayList<>();
        Set<TwinClassEntity> needLoadCommentActionsAlienProtected = new HashSet<>();
        Set<TwinClassEntity> needLoadCommentActionsSelfRestrict = new HashSet<>();
        TwinEntity twinEntity = null;
        TwinClassEntity twinClassEntity = null;
        UUID currentUserId = authService.getApiUser().getUserId();
        for (TwinCommentEntity twinComment : twinComments) {
            if (twinComment.getCommentActions() != null)
                continue;
            needLoad.add(twinComment);
            twinClassEntity = twinComment.getTwin().getTwinClass();
            if (twinComment.getCreatedByUserId().equals(currentUserId) && twinClassEntity.getCommentSelfActionsRestriction() == null)
                needLoadCommentActionsSelfRestrict.add(twinClassEntity);
            else if (twinClassEntity.getCommentAlienActionsProtectedByValidatorRules() == null || twinClassEntity.getCommentAlienActionsProtectedByPermission() == null)
                needLoadCommentActionsAlienProtected.add(twinClassEntity);
        }
        if (needLoad.isEmpty())
            return;
        if (!needLoadCommentActionsAlienProtected.isEmpty()) //bulk load
            loadClassCommentActionsAlienProtected(needLoadCommentActionsAlienProtected);
        if (!needLoadCommentActionsSelfRestrict.isEmpty()) //bulk load
            loadClassCommentActionsSelfRestrict(needLoadCommentActionsSelfRestrict);
        for (TwinCommentEntity twinComment : needLoad) { //now it's N+1 safe to do it loop because TwinClassEntities are already loaded with all necessary data
            loadCommentActions(twinComment);
        }
    }

    public void checkAllowed(TwinCommentEntity twinCommentEntity, TwinCommentAction action) throws ServiceException {
        if (!isAllowed(twinCommentEntity, action))
            throw new ServiceException(ErrorCodeTwins.TWIN_ACTION_NOT_AVAILABLE, "The action[" + action.name() + "] not available for comment[" + twinCommentEntity.getId() + "] on " + twinCommentEntity.getTwin().logNormal());
    }

    public boolean isAllowed(TwinCommentEntity twinCommentEntity, TwinCommentAction action) throws ServiceException {
        loadCommentActions(twinCommentEntity);
        return twinCommentEntity.getCommentActions().contains(action);
    }
}

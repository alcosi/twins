package org.twins.core.mappers.rest.related;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.action.ActionRestrictionReasonRestDTOMapper;
import org.twins.core.mappers.rest.attachment.AttachmentRestrictionRestDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.comment.CommentRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListRestDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListSubsetRestDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.factory.*;
import org.twins.core.mappers.rest.featurer.FeaturerParametrizedRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerTypeRestDTOMapper;
import org.twins.core.mappers.rest.history.HistoryTypeRestDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nRestDTOMapper;
import org.twins.core.mappers.rest.link.LinkRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.notification.*;
import org.twins.core.mappers.rest.permission.PermissionGroupRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionRestDTOMapper;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeGroupRestDTOMapper;
import org.twins.core.mappers.rest.projection.ProjectionTypeRestDTOMapper;
import org.twins.core.mappers.rest.scheduler.SchedulerRestDTOMapperV1;
import org.twins.core.mappers.rest.space.SpaceRoleDTOMapper;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;
import org.twins.core.mappers.rest.tier.TierRestDTOMapper;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.twinclass.*;
import org.twins.core.mappers.rest.twinflow.TransitionBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowBaseV1RestDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowFactoryRestDTOMapperV1;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinpointer.TwinPointerRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.mappers.rest.usergroup.UserGroupRestDTOMapper;
import org.twins.core.mappers.rest.validator.TwinValidatorSetRestDTOMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source of truth for the related-objects RestSimpleDTOMapper beans. Extracted from
 * RelatedObjectsRestDTOConverter so that other components (e.g. EntityRefRestDTOMapper converting a loaded
 * EntityRef into the DTO of its concrete entity) can resolve the mapper by entity class.
 * {@link #getMapper(Class)} serves the by-class lookup; direct getters serve the converter blocks.
 */
@Component
@Slf4j
@Getter
@RequiredArgsConstructor
public class EntityRestMapperRegistry {

    private final TwinClassRestDTOMapper twinClassRestDTOMapper;
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final LinkRestDTOMapper linkRestDTOMapper;
    private final UserRestDTOMapper userRestDTOMapper;
    private final UserGroupRestDTOMapper userGroupRestDTOMapper;
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    private final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;
    private final DataListRestDTOMapper dataListRestDTOMapper;
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    private final DataListSubsetRestDTOMapper dataListSubsetRestDTOMapper;
    private final SpaceRoleDTOMapper spaceRoleDTOMapper;
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;
    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;
    private final TwinflowFactoryRestDTOMapperV1 twinflowFactoryRestDTOMapperV1;
    private final TwinClassSchemaDTOMapper twinClassSchemaDTOMapper;
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;
    private final FactoryRestDTOMapper factoryRestDTOMapper;
    private final FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;
    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;
    private final FactoryPipelineStepRestDTOMapper factoryPipelineStepRestDTOMapper;
    private final FactoryMultiplierFilterRestDTOMapper factoryMultiplierFilterRestDTOMapper;
    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;
    private final FactoryTriggerRestDTOMapper factoryTriggerRestDTOMapper;
    private final FactoryConditionRestDTOMapper factoryConditionRestDTOMapper;
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FeaturerTypeRestDTOMapper featurerTypeRestDTOMapper;
    private final FaceRestDTOMapper faceRestDTOMapper;
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    private final CommentRestDTOMapper commentRestDTOMapper;
    private final I18nRestDTOMapper i18nRestDTOMapper;
    private final TierRestDTOMapper tierRestDTOMapper;
    private final AttachmentRestrictionRestDTOMapper attachmentRestrictionRestDTOMapper;
    private final TwinClassFreezeDTOMapper twinClassFreezeDTOMapper;
    private final TwinClassFieldRuleRestDTOMapper twinClassFieldRuleRestDTOMapper;
    private final ProjectionTypeGroupRestDTOMapper projectionTypeGroupRestDTOMapper;
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;
    private final SchedulerRestDTOMapperV1 schedulerRestDTOMapperV1;
    private final HistoryNotificationRecipientDTOMapperV1 historyNotificationRecipientDTOMapper;
    private final NotificationSchemaRestDTOMapper notificationSchemaRestDTOMapper;
    private final NotificationChannelRestDTOMapper notificationChannelRestDTOMapper;
    private final NotificationContextRestDTOMapper notificationContextRestDTOMapper;
    private final NotificationChannelEventRestDTOMapper notificationChannelEventRestDTOMapper;
    private final TwinValidatorSetRestDTOMapper twinValidatorSetRestDTOMapper;
    private final HistoryTypeRestDTOMapper historyTypeRestDTOMapper;
    private final ActionRestrictionReasonRestDTOMapper actionRestrictionReasonRestDTOMapper;
    private final TwinPointerRestDTOMapper twinPointerRestDTOMapper;
    private final FeaturerParametrizedRestDTOMapper featurerParametrizedRestDTOMapper;
    private final EntityRefRestDTOMapper entityRefRestDTOMapper;

    private final Map<Class<?>, RestSimpleDTOMapper<?, ?>> byEntityClass = new HashMap<>();
    private final Map<Class<?>, MapperMode> defaultShowModeByEntityClass = new HashMap<>();

    @PostConstruct
    void initByEntityClass() {
        register(TwinClassEntity.class, twinClassRestDTOMapper, TwinClassMode.DETAILED);
        register(TwinEntity.class, twinRestDTOMapperV2, TwinMode.DETAILED);
        register(TwinStatusEntity.class, twinStatusRestDTOMapper, StatusMode.DETAILED);
        register(LinkEntity.class, linkRestDTOMapper, LinkMode.DETAILED);
        register(TwinTriggerEntity.class, twinTriggerRestDTOMapper);
        register(UserEntity.class, userRestDTOMapper, UserMode.DETAILED);
        register(UserGroupEntity.class, userGroupRestDTOMapper, UserGroupMode.DETAILED);
        register(TwinflowTransitionEntity.class, transitionBaseV1RestDTOMapper);
        register(DataListEntity.class, dataListRestDTOMapper, DataListMode.DETAILED);
        register(DataListOptionEntity.class, dataListOptionRestDTOMapper, DataListOptionMode.DETAILED);
        register(DataListSubsetEntity.class, dataListSubsetRestDTOMapper, DataListSubsetMode.DETAILED);
        register(SpaceRoleEntity.class, spaceRoleDTOMapper);
        register(BusinessAccountEntity.class, businessAccountDTOMapper);
        register(PermissionGroupEntity.class, permissionGroupRestDTOMapper);
        register(PermissionEntity.class, permissionRestDTOMapper, PermissionMode.DETAILED);
        register(PermissionSchemaEntity.class, permissionSchemaRestDTOMapper, PermissionSchemaMode.DETAILED);
        register(TwinflowEntity.class, twinflowBaseV1RestDTOMapper);
        register(TwinflowFactoryEntity.class, twinflowFactoryRestDTOMapperV1);
        register(TwinClassSchemaEntity.class, twinClassSchemaDTOMapper, TwinClassSchemaMode.SHORT);
        register(TwinflowSchemaEntity.class, twinflowSchemaRestDTOMapper, TwinflowSchemaMode.SHORT);
        register(TwinFactoryEntity.class, factoryRestDTOMapper);
        register(TwinFactoryPipelineEntity.class, factoryPipelineRestDTOMapper);
        register(TwinFactoryConditionSetEntity.class, factoryConditionSetRestDTOMapper);
        register(TwinFactoryMultiplierEntity.class, factoryMultiplierRestDTOMapper);
        register(TwinFactoryBranchEntity.class, factoryBranchRestDTOMapper);
        register(TwinFactoryPipelineStepEntity.class, factoryPipelineStepRestDTOMapper);
        register(TwinFactoryMultiplierFilterEntity.class, factoryMultiplierFilterRestDTOMapper);
        register(TwinFactoryEraserEntity.class, factoryEraserRestDTOMapper);
        register(TwinFactoryTriggerEntity.class, factoryTriggerRestDTOMapper);
        register(TwinFactoryConditionEntity.class, factoryConditionRestDTOMapper);
        register(FeaturerEntity.class, featurerRestDTOMapper);
        register(FeaturerTypeEntity.class, featurerTypeRestDTOMapper);
        register(FaceEntity.class, faceRestDTOMapper);
        register(TwinClassFieldEntity.class, twinClassFieldRestDTOMapper, TwinClassFieldMode.DETAILED);
        register(TwinCommentEntity.class, commentRestDTOMapper);
        register(I18nEntity.class, i18nRestDTOMapper, I18nMode.DETAILED);
        register(TierEntity.class, tierRestDTOMapper);
        register(TwinAttachmentRestrictionEntity.class, attachmentRestrictionRestDTOMapper); // mapper has no mode binding
        register(TwinClassFreezeEntity.class, twinClassFreezeDTOMapper, TwinClassFreezeMode.DETAILED);
        register(TwinClassFieldRuleEntity.class, twinClassFieldRuleRestDTOMapper);
        register(ProjectionTypeGroupEntity.class, projectionTypeGroupRestDTOMapper); // mapper has no mode binding
        register(ProjectionTypeEntity.class, projectionTypeRestDTOMapper);
        register(SchedulerEntity.class, schedulerRestDTOMapperV1);
        register(HistoryNotificationRecipientEntity.class, historyNotificationRecipientDTOMapper);
        register(NotificationSchemaEntity.class, notificationSchemaRestDTOMapper);
        register(NotificationChannelEntity.class, notificationChannelRestDTOMapper);
        register(NotificationContextEntity.class, notificationContextRestDTOMapper);
        register(NotificationChannelEventEntity.class, notificationChannelEventRestDTOMapper);
        register(TwinValidatorSetEntity.class, twinValidatorSetRestDTOMapper);
        register(HistoryTypeEntity.class, historyTypeRestDTOMapper);
        register(ActionRestrictionReasonEntity.class, actionRestrictionReasonRestDTOMapper);
        register(TwinPointerEntity.class, twinPointerRestDTOMapper, TwinPointerMode.SHORT);
    }

    private void register(Class<?> entityClass, RestSimpleDTOMapper<?, ?> mapper) {
        register(entityClass, mapper, null);
    }

    private void register(Class<?> entityClass, RestSimpleDTOMapper<?, ?> mapper, MapperMode defaultShowMode) {
        RestSimpleDTOMapper<?, ?> previous = byEntityClass.put(entityClass, mapper);
        if (previous != null)
            log.error("Duplicate EntityRestMapperRegistry entry for entity class[{}]", entityClass.getName());
        if (defaultShowMode != null)
            defaultShowModeByEntityClass.put(entityClass, defaultShowMode);
    }

    /**
     * Mapper serving the given entity class, or null if the class has no registered mapper
     * (e.g. TwinClassFieldSearchEntity has no DTO/mapper yet).
     */
    public RestSimpleDTOMapper<?, ?> getMapper(Class<?> entityClass) {
        return byEntityClass.get(entityClass);
    }

    /**
     * SHORT show mode of the mapper serving the given entity class (the mode postponed entities are
     * rendered at, e.g. by EntityRefRestDTOMapper), or null if the mapper has no mode binding.
     */
    public MapperMode getShortMode(Class<?> entityClass) {
        return defaultShowModeByEntityClass.get(entityClass);
    }
}

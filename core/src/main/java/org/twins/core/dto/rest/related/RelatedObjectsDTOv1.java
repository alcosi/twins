package org.twins.core.dto.rest.related;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionDTOv1;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.comment.CommentDTOv1;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.i18n.I18nDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupDTOv1;
import org.twins.core.dto.rest.scheduler.SchedulerDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "RelatedObjectsV1")
public class RelatedObjectsDTOv1 {
    @Schema(description = "related statuses map", example = "{twin map}")
    public Map<UUID, TwinDTOv2> twinMap;

    @Schema(description = "related statuses map", example = "{twin status map}")
    public Map<UUID, TwinStatusDTOv1> statusMap;

    @Schema(description = "related users map", example = "{user map}")
    public Map<UUID, UserDTOv1> userMap;

    @Schema(description = "related users group map", example = "{user group map}")
    public Map<UUID, UserGroupDTOv1> userGroupMap;

    @Schema(description = "related twinClass map", example = "{twin class map}")
    public Map<UUID, TwinClassDTOv1> twinClassMap;

    @Schema(description = "related transitionsMap map", example = "{twin transition map}")
    public Map<UUID, TwinflowTransitionBaseDTOv1> transitionsMap;

    @Schema(description = "related datalist map", example = "{datalist map}")
    public Map<UUID, DataListDTOv1> dataListsMap;

    @Schema(description = "related datalistOption map", example = "{datalistOption map}")
    public Map<UUID, DataListOptionDTOv1> dataListsOptionMap;

    @Schema(description = "related space role map", example = "{space role map}")
    public Map<UUID, SpaceRoleDTOv1> spaceRoleMap;

    @Schema(description = "related business account map", example = "{business account map}")
    public Map<UUID, BusinessAccountDTOv1> businessAccountMap;

    @Schema(description = "related permission group map", example = "{permission group map}")
    public Map<UUID, PermissionGroupDTOv1> permissionGroupMap;

    @Schema(description = "related permission map", example = "{permission map}")
    public Map<UUID, PermissionDTOv1> permissionMap;

    @Schema(description = "related permission schema map", example = "{permission schema map}")
    public Map<UUID, PermissionSchemaDTOv1> permissionSchemaMap;

    @Schema(description = "related twinflow map", example = "{twinflow map}")
    public Map<UUID, TwinflowBaseDTOv1> twinflowMap;

    @Schema(description = "related twinflow schema map", example = "{twinflow schema map}")
    public Map<UUID, TwinflowSchemaDTOv1> twinflowSchemaMap;

    @Schema(description = "related link map", example = "{link map}")
    public Map<UUID, LinkDTOv1> linkMap;

    @Schema(description = "related factory map", example = "{factory map}")
    public Map<UUID, FactoryDTOv1> factoryMap;

    @Schema(description = "related factory pipeline map", example = "{factory pipeline map}")
    public Map<UUID, FactoryPipelineDTOv1> factoryPipelineMap;

    @Schema(description = "related factory multiplier map", example = "{factory multiplier map}")
    public Map<UUID, FactoryMultiplierDTOv1> factoryMultiplierMap;

    @Schema(description = "related factory conditionSet map", example = "{factory conditionSet map}")
    public Map<UUID, FactoryConditionSetDTOv1> factoryConditionSetMap;

    @Schema(description = "related twin class schema map", example = "{twin class schema map}")
    public Map<UUID, TwinClassSchemaDTOv1> twinClassSchemaMap;

    @Schema(description = "related comment map", example = "{comment map}")
    public Map<UUID, CommentDTOv1> commentMap;

    @Schema(description = "related featurer map", example = "{featurer map}")
    public Map<Integer, FeaturerDTOv1> featurerMap;

    @Schema(description = "related face map", example = "{face map}")
    public Map<UUID, FaceDTOv1> faceMap;

    @Schema(description = "related i18n map", example = "{face map}")
    public Map<UUID, I18nDTOv1> i18nMap;

    @Schema(description = "related class field map", example = "{class field map}")
    public Map<UUID, TwinClassFieldDTOv1> twinClassFieldMap;

    @Schema(description = "related attachment restriction map", example = "{attachment restriction map}")
    public Map<UUID, AttachmentRestrictionDTOv1> attachmentRestrictionMap;

    @Schema(description = "related tier map", example = "{tier map}")
    public Map<UUID, TierDTOv1> tierMap;

    @Schema(description = "related twinclass freeze map", example = "{twin class freeze map}")
    public Map<UUID, TwinClassFreezeDTOv1> twinClassFreezeMap;

    @Schema(description = "related field rules")
    public Map<UUID, TwinClassFieldRuleDTOv1> fieldRuleMap;

    @Schema(description = "related projection type group")
    public Map<UUID, ProjectionTypeGroupDTOv1> projectionTypeGroupMap;

    @Schema(description = "related projection type")
    public Map<UUID, ProjectionTypeDTOv1> projectionTypeMap;

    @Schema(description = "scheduler map")
    public Map<UUID, SchedulerDTOv1> schedulerMap;

    /**
     * Generated by GenerateRelatedObjectsTask
     */
    public <T> T get(Class<T> relatedObjectClass, Object id) {
        if (relatedObjectClass == TwinDTOv2.class) {
            return (T) twinMap.get(id);
        } else if (relatedObjectClass == TwinStatusDTOv1.class) {
            return (T) statusMap.get(id);
        } else if (relatedObjectClass == UserDTOv1.class) {
            return (T) userMap.get(id);
        } else if (relatedObjectClass == UserGroupDTOv1.class) {
            return (T) userGroupMap.get(id);
        } else if (relatedObjectClass == TwinClassDTOv1.class) {
            return (T) twinClassMap.get(id);
        } else if (relatedObjectClass == TwinflowTransitionBaseDTOv1.class) {
            return (T) transitionsMap.get(id);
        } else if (relatedObjectClass == DataListDTOv1.class) {
            return (T) dataListsMap.get(id);
        } else if (relatedObjectClass == DataListOptionDTOv1.class) {
            return (T) dataListsOptionMap.get(id);
        } else if (relatedObjectClass == SpaceRoleDTOv1.class) {
            return (T) spaceRoleMap.get(id);
        } else if (relatedObjectClass == BusinessAccountDTOv1.class) {
            return (T) businessAccountMap.get(id);
        } else if (relatedObjectClass == PermissionGroupDTOv1.class) {
            return (T) permissionGroupMap.get(id);
        } else if (relatedObjectClass == PermissionDTOv1.class) {
            return (T) permissionMap.get(id);
        } else if (relatedObjectClass == PermissionSchemaDTOv1.class) {
            return (T) permissionSchemaMap.get(id);
        } else if (relatedObjectClass == TwinflowBaseDTOv1.class) {
            return (T) twinflowMap.get(id);
        } else if (relatedObjectClass == TwinflowSchemaDTOv1.class) {
            return (T) twinflowSchemaMap.get(id);
        } else if (relatedObjectClass == LinkDTOv1.class) {
            return (T) linkMap.get(id);
        } else if (relatedObjectClass == FactoryDTOv1.class) {
            return (T) factoryMap.get(id);
        } else if (relatedObjectClass == FactoryPipelineDTOv1.class) {
            return (T) factoryPipelineMap.get(id);
        } else if (relatedObjectClass == FactoryMultiplierDTOv1.class) {
            return (T) factoryMultiplierMap.get(id);
        } else if (relatedObjectClass == FactoryConditionSetDTOv1.class) {
            return (T) factoryConditionSetMap.get(id);
        } else if (relatedObjectClass == TwinClassSchemaDTOv1.class) {
            return (T) twinClassSchemaMap.get(id);
        } else if (relatedObjectClass == CommentDTOv1.class) {
            return (T) commentMap.get(id);
        } else if (relatedObjectClass == FeaturerDTOv1.class) {
            return (T) featurerMap.get(id);
        } else if (relatedObjectClass == FaceDTOv1.class) {
            return (T) faceMap.get(id);
        } else if (relatedObjectClass == I18nDTOv1.class) {
            return (T) i18nMap.get(id);
        } else if (relatedObjectClass == TwinClassFieldDTOv1.class) {
            return (T) twinClassFieldMap.get(id);
        } else if (relatedObjectClass == AttachmentRestrictionDTOv1.class) {
            return (T) attachmentRestrictionMap.get(id);
        } else if (relatedObjectClass == TierDTOv1.class) {
            return (T) tierMap.get(id);
        } else if (relatedObjectClass == TwinClassFreezeDTOv1.class) {
            return (T) twinClassFreezeMap.get(id);
        } else if (relatedObjectClass == TwinClassFieldRuleDTOv1.class) {
            return (T) fieldRuleMap.get(id);
        } else if (relatedObjectClass == ProjectionTypeGroupDTOv1.class) {
            return (T) projectionTypeGroupMap.get(id);
        } else if (relatedObjectClass == ProjectionTypeDTOv1.class) {
            return (T) projectionTypeMap.get(id);
        } else if (relatedObjectClass == SchedulerDTOv1.class) {
            return (T) schedulerMap.get(id);
        } else {
            return null;
        }
    }
}

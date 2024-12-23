package org.twins.core.dto.rest.related;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv2;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowTransitionBaseDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "RelatedObjectsV1")
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
    public Map<UUID, PermissionSchemaDTOv2> permissionSchemaMap;

    @Schema(description = "related twinflow map", example = "{twinflow map}")
    public Map<UUID, TwinflowBaseDTOv1> twinflowMap;

    @Schema(description = "related factory map", example = "{factory map}")
    public Map<UUID, FactoryDTOv1> factoryMap;

    @Schema(description = "related factory pipeline map", example = "{factory pipeline map}")
    public Map<UUID, FactoryPipelineDTOv1> factoryPipelineMap;

    @Schema(description = "related featurer map", example = "{featurer map}")
    public Map<Integer, FeaturerDTOv1> featurerMap;
}

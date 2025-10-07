package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.twinclass.OwnerType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassBaseV1")
public class TwinClassBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID id;

    @Schema(description = "key", example = "PROJECT")
    public String key;

    @Schema(description = "name", example = "Project")
    public String name;

    @Schema(description = "description", example = "Projects business objects")
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "availabilityId of twin class", example = DTOExamples.TWIN_CLASS_AVAILABILITY_ID)
    public UUID twinClassAvailabilityId;

    @Schema(description = "iconDark", example = "http://twins.org/t/class/project.png")
    public String iconDark;

    @Schema(description = "iconLight", example = "http://twins.org/t/class/project.png")
    public String iconLight;

    @Schema(description = "if class is abstract no twin of it can be created. Some child class must be used")
    public Boolean abstractClass;

    @Schema(description = "head class id or empty if class is not linked to any head", example = DTOExamples.TWIN_CLASS_HEAD_CLASS_ID)
    public UUID headClassId;

    @Schema(description = "some markers for twins. Are domain level and not editable by user")
    public UUID markersDataListId;

    @Schema(description = "some tags for twins. Can be business account level and editable by user")
    public UUID tagsDataListId;

    @Schema(description = "twin class owner type")
    public OwnerType ownerType;

    @Schema(description = "extends class id or empty if class is not linked to any classes")
    public UUID extendsClassId;

    @Schema(description = "if true, take the twinflow scheme in space")
    public Boolean twinflowSchemaSpace;

    @Schema(description = "")
    public Boolean twinClassSchemaSpace;

    @Schema(description = "if true, take the permission scheme in space")
    public Boolean permissionSchemaSpace;

    @Schema(description = "")
    public Boolean aliasSpace;

    @Schema(description = "head hunter featurer id")
    public Integer headHunterFeaturerId;

    @Schema(description = "head hunter featurer params")
    public Map<String, String> headHunterParams;

    @Schema(description = "view permission id")
    public UUID viewPermissionId;

    @Schema(description = "create permission id")
    public UUID createPermissionId;

    @Schema(description = "edit permission id")
    public UUID editPermissionId;

    @Schema(description = "delete permission id")
    public UUID deletePermissionId;

    @Schema(description = "uuid of name in I18n table")
    public UUID nameI18nId;

    @Schema(description = "uuid of description in I18n table")
    public UUID descriptionI18nId;

    @Schema(description = "twin display page pointer")
    public UUID pageFaceId;

    @Schema(description = "breadcrumbs face id")
    public UUID breadCrumbsFaceId;

    @Schema(description = "inherited page face id")
    public UUID inheritedPageFaceId;

    @Schema(description = "inherited breadcrumbs face id")
    public UUID inheritedBreadCrumbsFaceId;

    @Schema(description = "assignee required")
    public Boolean assigneeRequired;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "external properties")
    public Map<String, String> externalProperties;

    @Schema(description = "External JSON data")
    public Map<String, Object> externalJson;

    @Schema(description = "segment class id")
    public Set<UUID> segmentClassIds;

    @Schema(description = "Class fields id list")
    public Set<UUID> fieldIds;

    @Schema(description = "List of status id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> statusList;

    @Schema(description = "List of marker id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> markerList;
}

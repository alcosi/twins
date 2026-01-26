package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.dto.rest.link.LinkDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.enums.twinclass.OwnerType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode()
@Accessors(chain = true)
@Schema(name =  "TwinClassV1")
public class TwinClassDTOv1 {
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

    @Schema(description = "freeze of twin class", example = DTOExamples.TWIN_CLASS_FREEZE_ID)
    public UUID twinClassFreezeId;

    @Schema(description = "iconDark", example = "http://twins.org/t/class/project.png")
    public String iconDark;

    @Schema(description = "iconLight", example = "http://twins.org/t/class/project.png")
    public String iconLight;

    @Schema(description = "if class is abstract no twin of it can be created. Some child class must be used")
    public Boolean abstractClass;

    @RelatedObject(type = TwinClassDTOv1.class, name = "headClass")
    @Schema(description = "head class id or empty if class is not linked to any head", example = DTOExamples.TWIN_CLASS_HEAD_CLASS_ID)
    public UUID headClassId;

    @RelatedObject(type = DataListDTOv1.class, name = "markersDataList")
    @Schema(description = "some markers for twins. Are domain level and not editable by user")
    public UUID markersDataListId;

    @RelatedObject(type = DataListDTOv1.class, name = "tagsDataList")
    @Schema(description = "some tags for twins. Can be business account level and editable by user")
    public UUID tagsDataListId;

    @Schema(description = "twin class owner type")
    public OwnerType ownerType;

    @RelatedObject(type = TwinClassDTOv1.class, name = "extendsClass")
    @Schema(description = "extends class id (direct) or empty if class is not extends any classes")
    public UUID extendsClassId;

    @Schema(description = "extends class id set ")
    public Set<UUID> extendsClassIdSet;

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

    @RelatedObject(type = PermissionDTOv1.class, name = "viewPermission")
    @Schema(description = "view permission id")
    public UUID viewPermissionId;

    @RelatedObject(type = PermissionDTOv1.class, name = "createPermission")
    @Schema(description = "create permission id")
    public UUID createPermissionId;

    @RelatedObject(type = PermissionDTOv1.class, name = "editPermission")
    @Schema(description = "edit permission id")
    public UUID editPermissionId;

    @RelatedObject(type = PermissionDTOv1.class, name = "deletePermission")
    @Schema(description = "delete permission id")
    public UUID deletePermissionId;

    @Schema(description = "uuid of name in I18n table")
    public UUID nameI18nId;

    @Schema(description = "uuid of description in I18n table")
    public UUID descriptionI18nId;

    @RelatedObject(type = FaceDTOv1.class, name = "pageFace")
    @Schema(description = "twin display page pointer")
    public UUID pageFaceId;

    @RelatedObject(type = FaceDTOv1.class, name = "breadCrumbsFace")
    @Schema(description = "breadcrumbs face id")
    public UUID breadCrumbsFaceId;

    @RelatedObject(type = FaceDTOv1.class, name = "inheritedPageFace")
    @Schema(description = "inherited page face id")
    public UUID inheritedPageFaceId;

    @RelatedObject(type = FaceDTOv1.class, name = "inheritedBreadCrumbsFace")
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

    @RelatedObject(type = TwinClassDTOv1.class, name = "segmentClassList")
    @Schema(description = "segment class id")
    public Set<UUID> segmentClassIds;

    @Schema(description = "")
    public Boolean segment;

    @Schema(description = "")
    public Boolean hasSegment;

    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "fieldList")
    @Schema(description = "Class fields id list")
    public Set<UUID> fieldIds;

    @RelatedObject(type = TwinStatusDTOv1.class, name = "statusList")
    @Schema(description = "List of status id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> statusIds;

    @RelatedObject(type = DataListOptionDTOv1.class, name = "markerList")
    @Schema(description = "List of marker id." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> markerIds;

    @Schema()
    public Map<UUID, LinkDTOv1> forwardLinkMap;

    @Schema()
    public Map<UUID, LinkDTOv1> backwardLinkMap;

    @Schema(description = "Map of markers." + DTOExamples.LAZY_RELATION_MODE_ON)
    public Map<UUID, DataListOptionDTOv1> markerMap;

    @Deprecated
    @Schema(description = "Map of tags." + DTOExamples.LAZY_RELATION_MODE_ON)
    public Map<UUID, DataListOptionDTOv1> tagMap;
}

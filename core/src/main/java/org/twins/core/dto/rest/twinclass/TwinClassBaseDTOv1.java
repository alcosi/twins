package org.twins.core.dto.rest.twinclass;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.Map;
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

    @Schema(description = "logo", example = "http://twins.org/t/class/project.png")
    public String logo;

    @Schema(description = "if class is abstract no twin of it can be created. Some child class must be used")
    public Boolean abstractClass;

    @Schema(description = "head class id or empty if class is not linked to any head", example = DTOExamples.TWIN_CLASS_HEAD_CLASS_ID)
    public UUID headTwinClassId;

    @Schema(description = "some markers for twins. Are domain level and not editable by user")
    public UUID markersDataListId;

    @Schema(description = "some tags for twins. Can be business account level and editable by user")
    public UUID tagsDataListId;

    @Schema(description = "twin class owner type")
    public TwinClassEntity.OwnerType ownerType;

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

    @Schema(description = "uuid of name in I18n table")
    public UUID nameI18nId;

    @Schema(description = "uuid of description in I18n table")
    public UUID descriptionI18nId;


}

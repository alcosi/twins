package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.common.BasicUpdateOperationDTOv1;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassUpdateV1")
public class TwinClassUpdateDTOv1 {
    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "unique key within the domain", example = DTOExamples.TWIN_CLASS_KEY)
    public String key;

    @Schema(description = "name")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "[optional] description")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "freeze of twin class", example = DTOExamples.TWIN_CLASS_FREEZE_ID)
    public UUID twinClassFreezeId;

    @Schema(description = "[optional] an id of head hunter featurer. The field has a sense only if headTwinClassId filled", example = "")
    public Integer headHunterFeaturerId;

    @Schema(description = "[optional] head hunter featurer params", example = "")
    public HashMap<String, String> headHunterParams;

    @Schema(description = "[optional] if true, then not twin of given class can be created. Abstract classes must be extended", example = "false")
    public Boolean abstractClass;

    @Schema(description = "[optional] if true then twins of current class can have own permission_schema and this schema will cover children twins", example = "false")
    public Boolean permissionSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class can have own twinflow_schema and this schema will cover children twins", example = "false")
    public Boolean twinflowSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class can have own twin_class_schema and this schema will cover children twins", example = "false")
    public Boolean twinClassSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class must have own alias key and this key will be used to generate alias for children twins", example = "false")
    public Boolean aliasSpace;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to view twins of given class. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID viewPermissionId;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to create twins of given class. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID createPermissionId;

    @Schema(description = "[optional] owner typ of class")
    public OwnerType ownerType;

    @Schema(description = "[optional] is assignee required")
    public Boolean assigneeRequired;

    @Schema(description = "[optional] is segment")
    public Boolean segment;

    @Schema(description = "[optional] is unique name")
    public Boolean uniqueName;

    @Schema(description = "[optional] external id")
    public String externalId;

    @Schema(description = "[optional] external properties")
    public Map<String, String> externalProperties;

    @Schema(description = "[optional] External JSON data", example = "{\"key1\": \"value1\", \"key2\": 123}")
    public Map<String, Object> externalJson;

    @Schema(description = "[optional] should be filled on change marker data list id")
    public BasicUpdateOperationDTOv1 markerDataListUpdate;

    @Schema(description = "[optional] should be filled on change tag data list id")
    public BasicUpdateOperationDTOv1 tagDataListUpdate;

    @Schema(description = "[optional] should be filled on change flavor data list id")
    public BasicUpdateOperationDTOv1 flavorDataListUpdate;

    @Schema(description = "[optional] should be filled on change extends twins class id")
    public BasicUpdateOperationDTOv1 extendsTwinClassUpdate;

    @Schema(description = "[optional] should be filled on change extends twins class id")
    public BasicUpdateOperationDTOv1 headTwinClassUpdate;
}

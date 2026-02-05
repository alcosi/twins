package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.twinclass.OwnerType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassSaveRqV1")
public class TwinClassSaveRqDTOv1 extends Request {
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

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to edit twins of given class. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID editPermissionId;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to delete twins of given class. Use ffffffff-ffff-ffff-ffff-ffffffffffff for nullify value", example = "")
    public UUID deletePermissionId;

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
}

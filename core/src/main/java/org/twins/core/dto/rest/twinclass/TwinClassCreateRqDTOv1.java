package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassCreateRqV1")
public class TwinClassCreateRqDTOv1 extends Request {

    @Schema(description = "unique key within the domain", example = DTOExamples.TWIN_CLASS_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.TWIN_CLASS_NAME)
    public String name;

    @Schema(description = "[optional] description", example = DTOExamples.TWIN_CLASS_DESCRIPTION)
    public String description;

    @Schema(description = "[optional] link to head (parent) class. It should be used in case, when twins of some class can not exist without some parent twin. Example: Task and Sub-task", example = "")
    public UUID headTwinClassId;

    @Schema(description = "[optional] link to extends class. All fields and links will be valid for current class.", example = "")
    public UUID extendsTwinClassId;

    @Schema(description = "[optional] if true, then not twin of given class can be created. Abstract classes must be extended", example = "false")
    public boolean abstractClass;

    @Schema(description = "[optional] url for class UI logo", example = "")
    public String logo;

    @Schema(description = "[optional] if true then twins of current class can have own permission_schema and this schema will cover children twins", example = "false")
    public boolean permissionSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class can have own twinflow_schema and this schema will cover children twins", example = "false")
    public boolean twinflowSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class can have own twin_class_schema and this schema will cover children twins", example = "false")
    public boolean twinClassSchemaSpace;

    @Schema(description = "[optional] if true then twins of current class must have own alias key and this key will be used to generate alias for children twins", example = "false")
    public boolean aliasSpace;

    @Schema(description = "[optional] id of linked marker list. Markers in some cases similar to secondary statuses", example = "")
    public UUID markerDataListId;

    @Schema(description = "[optional] id of linked tags cloud. Tags differ from markers in that new tags can be added to the cloud by the users themselves. " +
            "And the list of markers is configured only by the domain manager", example = "")
    public UUID tagDataListId;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to view twins of given class", example = "")
    public UUID viewPermissionId;
}

package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldVisibility;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFieldV1")
public class TwinClassFieldDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.TWIN_CLASS_FIELD_KEY)
    public String key;

    @Schema(description = "name", example = "Serial number")
    public String name;

    @Schema(description = "required", example = "true")
    public boolean required;

    @Schema(description = "description", example = "")
    public String description;

    @Schema(description = "field descriptor", example = "")
    public TwinClassFieldDescriptorDTO descriptor;

    @Schema(description = "twin class id", example = "")
    public UUID twinClassId;

    @Schema(description = "name i18n id", example = "")
    public UUID nameI18nId;

    @Schema(description = "description i18n id", example = "")
    public UUID descriptionI18nId;

    @Schema(description = "field typer featurer id", example = "")
    public Integer fieldTyperFeaturerId;

    @Schema(description = "field typer params", example = "")
    public Map<String, String> fieldTyperParams;

    @Schema(description = "view permission id", example = "")
    public UUID viewPermissionId;

    @Schema(description = "edit permission id", example = "")
    public UUID editPermissionId;

    @Schema(description = "external id", example = "")
    public String externalId;

    @Schema(description = "field visibility", example = "PUBLIC")
    public TwinClassFieldVisibility twinClassFieldVisibilityId;
}

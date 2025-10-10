package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.List;
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
    @RelatedObject(type = TwinClassBaseDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "name i18n id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "nameI18n")
    public UUID nameI18nId;

    @Schema(description = "description i18n id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "descriptionI18n")
    public UUID descriptionI18nId;

    @Schema(description = "field typer featurer id", example = "")
    public Integer fieldTyperFeaturerId;

    @Schema(description = "field typer params", example = "")
    public Map<String, String> fieldTyperParams;

    @Schema(description = "twin sorter featurer id", example = "")
    public Integer twinSorterFeaturerId;

    @Schema(description = "twin sorter params", example = "")
    public Map<String, String> twinSorterParams;

    @Schema(description = "view permission id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "viewPermission")
    public UUID viewPermissionId;

    @Schema(description = "edit permission id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "editPermission")
    public UUID editPermissionId;

    @Schema(description = "external id", example = "")
    public String externalId;

    @Schema(description = "external id", example = "")
    public Boolean system;

    @Schema(description = "external properties")
    public Map<String, String> externalProperties;

    @Schema(description = "frontend validation error", example = "")
    public String feValidationError;

    @Schema(description = "backend validation error", example = "")
    public String beValidationError;

    @Schema(description = "frontend validation error i18n id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "feValidationErrorI18n")
    public UUID feValidationErrorI18nId;

    @Schema(description = "backend validation error i18n id", example = "")
    @RelatedObject(type = TwinDTOv2.class, name = "beValidationErrorI18n")
    public UUID beValidationErrorI18nId;

    @Schema(description = "rules bundles associated with this field")
    public List<TwinClassFieldRuleDTOv1> fieldRules;

}



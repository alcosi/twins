package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Map;
import java.util.Set;
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
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "name i18n id", example = "")
    public UUID nameI18nId;

    @Schema(description = "description i18n id", example = "")
    public UUID descriptionI18nId;

    @Schema(description = "field typer featurer id", example = "")
    @RelatedObject(type = FeaturerDTOv1.class, name = "fieldTyperFeaturer")
    public Integer fieldTyperFeaturerId;

    @Schema(description = "field typer params", example = "")
    public Map<String, String> fieldTyperParams;

    @Schema(description = "twin sorter featurer id", example = "")
    public Integer twinSorterFeaturerId;

    @Schema(description = "twin sorter params", example = "")
    public Map<String, String> twinSorterParams;

    @Schema(description = "view permission id", example = "")
    @RelatedObject(type = PermissionDTOv1.class, name = "viewPermission")
    public UUID viewPermissionId;

    @Schema(description = "edit permission id", example = "")
    @RelatedObject(type = PermissionDTOv1.class, name = "editPermission")
    public UUID editPermissionId;

    @Schema(description = "external id", example = "")
    public String externalId;

    @Schema(description = "is system field", example = "")
    public Boolean system;

    @Schema(description = "is dependent field", example = "")
    public Boolean dependent;

    @Schema(description = " has dependent fields", example = "")
    public Boolean hasDependentFields;

    @Schema(description = "external properties")
    public Map<String, String> externalProperties;

    @Schema(description = "frontend validation error", example = "")
    public String feValidationError;

    @Schema(description = "backend validation error", example = "")
    public String beValidationError;

    @Schema(description = "frontend validation error i18n id", example = "")
    public UUID feValidationErrorI18nId;

    @Schema(description = "backend validation error i18n id", example = "")
    public UUID beValidationErrorI18nId;

    @Schema(description = "field rule ids", example = "")
    public Set<UUID> ruleIds;

    @Schema(description = "order", example = "")
    public Integer order;

    @Schema(name = "is projection field", example = "")
    private Boolean projectionField;

    @Schema(name = "has projected fields", example = "")
    private Boolean hasProjectedFields;
}



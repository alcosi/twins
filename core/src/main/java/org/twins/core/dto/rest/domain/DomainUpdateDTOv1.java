package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;


@Data
@Accessors(chain  = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DomainUpdateV1")
public class DomainUpdateDTOv1 extends DomainSaveDTOv1 {
    @Schema(description = "business account initiator featurer id", example = DTOExamples.FEATURER_ID)
    public Integer businessAccountInitiatorFeaturerId;

    @Schema(description = "business account initiator params", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> businessAccountInitiatorParams;

    @Schema(description = "user group manager featurer id", example = DTOExamples.FEATURER_ID)
    public Integer userGroupManagerFeaturerId;

    @Schema(description = "user group manager params", example = DTOExamples.FEATURER_PARAM)
    public HashMap<String, String> userGroupManagerParams;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "twin class schema id", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    public UUID twinClassSchemaId;

    @Schema(description = "business account template twin id", example = DTOExamples.TWIN_ID)
    public UUID businessAccountTemplateTwinId;

    @Schema(description = "default iter id", example = DTOExamples.TIER_ID)
    public UUID defaultTierId;

    @Schema(description = "domain user template twin id", example = DTOExamples.TWIN_ID)
    public UUID domainUserTemplateTwinId;

    @Schema(description = "Icon dark resource id", example = DTOExamples.RESOURCE_ID)
    public UUID iconDarkResourceId;

    @Schema(description = "Icon light resource id", example = DTOExamples.RESOURCE_ID)
    public UUID iconLightResourceId;

    @Schema(description = "Navbar face id", example = DTOExamples.FACE_ID)
    public UUID navbarFaceId;
}

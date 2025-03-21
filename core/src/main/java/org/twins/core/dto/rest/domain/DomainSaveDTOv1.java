package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainType;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainSaveV1")
public class DomainSaveDTOv1 {
    //todo disable for edit
    @Schema(description = "will be used for url generation and for twins aliases", example = DTOExamples.DOMAIN_KEY)
    private String key;

    @Schema(description = "name", example = DTOExamples.DOMAIN_NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DOMAIN_DESCRIPTION)
    public String description;

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

    @Schema(description = "default locale for domain [en/de/by]", example = DTOExamples.LOCALE)
    public String defaultLocale;

    @Schema(description = "default iter id", example = DTOExamples.TIER_ID)
    public UUID defaultTierId;

    @Schema(description = "domain user template twin id", example = DTOExamples.TWIN_ID)
    public UUID domainUserTemplateTwinId;

    @Schema(description = "Icon dark resource id", example = DTOExamples.RESOURCE_ID)
    public UUID iconDarkResourceId;

    @Schema(description = "Icon light resource id", example = DTOExamples.RESOURCE_ID)
    public UUID iconLightResourceId;

    //todo disable for edit
    @Schema(description = "type [basic/b2b]", example = DTOExamples.DOMAIN_TYPE)
    private DomainType type;

    @Schema(description = "Resource storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    public UUID resourceStorageId;

    @Schema(description = "Attachment storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    public UUID attachmentStorageId;

    @Schema(description = "Navbar face id", example = DTOExamples.FACE_ID)
    public UUID navbarFaceId;
}

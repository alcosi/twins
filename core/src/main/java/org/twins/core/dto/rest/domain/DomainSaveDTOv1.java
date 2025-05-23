package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DomainSaveV1")
public class DomainSaveDTOv1 {
    @Schema(description = "name", example = DTOExamples.DOMAIN_NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DOMAIN_DESCRIPTION)
    public String description;

    @Schema(description = "default locale for domain [en/de/by]", example = DTOExamples.LOCALE)
    public String defaultLocale;

    @Schema(description = "Resource storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    public UUID resourceStorageId;

    @Schema(description = "Attachment storage type", example = DTOExamples.RESOURCE_STORAGE_ID)
    public UUID attachmentStorageId;
}

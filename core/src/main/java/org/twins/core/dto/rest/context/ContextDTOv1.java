package org.twins.core.dto.rest.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ContextV1")
public class ContextDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "name i18n id", example = "")
    public UUID nameI18nId;

    @Schema(description = "description i18n id", example = "")
    public UUID descriptionI18nId;
}

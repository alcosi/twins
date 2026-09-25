package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "NotificationSchemaUpdateV1")
public class NotificationSchemaUpdateDTOv1 {
    @NotNull
    @Schema(description = "notification schema id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "name i18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "description i18n")
    public I18nSaveDTOv1 descriptionI18n;
}

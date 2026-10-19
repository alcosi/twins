package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientUpdateV1")
public class HistoryNotificationRecipientUpdateDTOv1 {
    @NotNull
    @Schema(description = "history notification recipient id")
    public UUID id;

    @Schema(description = "nameI18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "descriptionI18n")
    public I18nSaveDTOv1 descriptionI18n;
}

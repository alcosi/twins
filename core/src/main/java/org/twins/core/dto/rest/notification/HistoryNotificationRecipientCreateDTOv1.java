package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nHasTranslation;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCreateV1")
public class HistoryNotificationRecipientCreateDTOv1 {
    @NotNull
    @I18nHasTranslation
    @Schema(description = "nameI18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "descriptionI18n")
    public I18nSaveDTOv1 descriptionI18n;
}

package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCreateV1")
public class HistoryNotificationRecipientCreateDTOv1 {
    @Schema(description = "nameI18n")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "descriptionI18n")
    public I18nSaveDTOv1 descriptionI18n;
}

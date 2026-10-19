package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationCreateV1")
public class HistoryNotificationCreateDTOv1 {
    @NotBlank
    @Schema(description = "history type id")
    public String historyTypeId;

    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    public UUID twinClassFieldId;

    @Schema(description = "twin validator set id")
    public UUID twinValidatorSetId;

    @Schema(description = "twin validator set invert")
    public Boolean twinValidatorSetInvert;

    @NotNull
    @Schema(description = "notification schema id")
    public UUID notificationSchemaId;

    @NotNull
    @Schema(description = "history notification recipient id")
    public UUID historyNotificationRecipientId;

    @NotNull
    @Schema(description = "notification channel event id")
    public UUID notificationChannelEventId;

    @Schema(description = "is active")
    public Boolean active;
}

package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorCreateV1")
public class HistoryNotificationRecipientCollectorCreateDTOv1 {
    @NotNull
    @Schema(description = "recipient id")
    public UUID recipientId;

    @NotNull
    @Schema(description = "recipient resolver featurer id")
    public Integer recipientResolverFeaturerId;

    @NotNull
    @Schema(description = "recipient resolver params")
    public HashMap<String, String> recipientResolverParams;

    @Schema(description = "exclude")
    public Boolean exclude;
}

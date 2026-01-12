package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorV1")
public class HistoryNotificationRecipientCollectorDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "recipient id")
    public UUID recipientId;

    @Schema(description = "recipient resolver featurer id")
    public Integer recipientResolverFeaturerId;

    @Schema(description = "recipient resolver params")
    public Map<String, String> recipientResolverParams;

    @Schema(description = "exclude")
    public Boolean exclude;
}

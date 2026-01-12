package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorSearchV1")
public class HistoryNotificationRecipientCollectorSearchDTOv1 {
    @Schema(description = "history notification recipient collector id list")
    public Set<UUID> idList;

    @Schema(description = "history notification recipient collector id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "history notification recipient id list")
    public Set<UUID> recipientIdList;

    @Schema(description = "history notification recipient id exclude list")
    public Set<UUID> recipientIdExcludeList;

    @Schema(description = "recipient resolver featurer id list")
    public Set<Integer> recipientResolverFeaturerIdList;

    @Schema(description = "recipient resolver featurer id exclude list")
    public Set<Integer> recipientResolverFeaturerIdExcludeList;

    @Schema(description = "exclude")
    public Ternary exclude;
}

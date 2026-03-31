package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientSearchV1")
public class HistoryNotificationRecipientSearchDTOv1 {
    @Schema(description = "idList")
    public Set<UUID> idList;

    @Schema(description = "idExcludeList")
    public Set<UUID> idExcludeList;

    @Schema(description = "nameLikeList")
    public Set<String> nameLikeList;

    @Schema(description = "nameNotLikeList")
    public Set<String> nameNotLikeList;

    @Schema(description = "descriptionLikeList")
    public Set<String> descriptionLikeList;

    @Schema(description = "descriptionNotLikeList")
    public Set<String> descriptionNotLikeList;
}

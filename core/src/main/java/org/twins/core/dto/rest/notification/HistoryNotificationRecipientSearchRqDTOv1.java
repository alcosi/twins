package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientSearchRqV1")
public class HistoryNotificationRecipientSearchRqDTOv1 extends Request {
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

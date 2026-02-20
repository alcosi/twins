package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationSearchRqV1")
public class HistoryNotificationSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "history type id list")
    public Set<String> historyTypeIdList;

    @Schema(description = "history type id exclude list")
    public Set<String> historyTypeIdExcludeList;

    @Schema(description = "twin class id list")
    public Set<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "twin class field id list")
    public Set<UUID> twinClassFieldIdList;

    @Schema(description = "twin class field id exclude list")
    public Set<UUID> twinClassFieldIdExcludeList;

    @Schema(description = "twin validator set id list")
    public Set<UUID> twinValidatorSetIdList;

    @Schema(description = "twin validator set id exclude list")
    public Set<UUID> twinValidatorSetIdExcludeList;

    @Schema(description = "twin validator set invert")
    public Ternary twinValidatorSetInvert;

    @Schema(description = "notification schema id list")
    public Set<UUID> notificationSchemaIdList;

    @Schema(description = "notification schema id exclude list")
    public Set<UUID> notificationSchemaIdExcludeList;

    @Schema(description = "history notification recipient id list")
    public Set<UUID> historyNotificationRecipientIdList;

    @Schema(description = "history notification recipient id exclude list")
    public Set<UUID> historyNotificationRecipientIdExcludeList;

    @Schema(description = "notification channel event id list")
    public Set<UUID> notificationChannelEventIdList;

    @Schema(description = "notification channel event id exclude list")
    public Set<UUID> notificationChannelEventIdExcludeList;
}

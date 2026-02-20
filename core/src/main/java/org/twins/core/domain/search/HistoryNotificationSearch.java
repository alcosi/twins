package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class HistoryNotificationSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<String> historyTypeIdList;
    public Set<String> historyTypeIdExcludeList;
    public Set<UUID> twinClassIdList;
    public Set<UUID> twinClassIdExcludeList;
    public Set<UUID> twinClassFieldIdList;
    public Set<UUID> twinClassFieldIdExcludeList;
    public Set<UUID> twinValidatorSetIdList;
    public Set<UUID> twinValidatorSetIdExcludeList;
    public Ternary twinValidatorSetInvert;
    public Set<UUID> notificationSchemaIdList;
    public Set<UUID> notificationSchemaIdExcludeList;
    public Set<UUID> historyNotificationRecipientIdList;
    public Set<UUID> historyNotificationRecipientIdExcludeList;
    public Set<UUID> notificationChannelEventIdList;
    public Set<UUID> notificationChannelEventIdExcludeList;
}

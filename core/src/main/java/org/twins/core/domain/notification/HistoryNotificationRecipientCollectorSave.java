package org.twins.core.domain.notification;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryNotificationRecipientCollectorSave {
    public UUID recipientId;
    public Integer recipientResolverFeaturerId;
    public HashMap<String, String> recipientResolverParams;
    public Boolean exclude;
}

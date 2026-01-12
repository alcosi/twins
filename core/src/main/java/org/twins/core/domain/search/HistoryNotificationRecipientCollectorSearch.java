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
public class HistoryNotificationRecipientCollectorSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> recipientIdList;
    public Set<UUID> recipientIdExcludeList;
    public Set<Integer> recipientResolverFeaturerIdList;
    public Set<Integer> recipientResolverFeaturerIdExcludeList;
    public Ternary exclude;
}

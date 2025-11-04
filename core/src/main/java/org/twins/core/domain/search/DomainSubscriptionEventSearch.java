package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class DomainSubscriptionEventSearch {

    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> domainIdList;
    private Set<UUID> domainIdExcludeList;
    private Set<String> subscriptionEventTypeList;
    private Set<String> subscriptionEventTypeExcludeList;
    private Set<Integer> dispatcherFeaturerIdList;
    private Set<Integer> dispatcherFeaturerIdExcludeList;
}


package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SchedulerSearch {
    public Set<UUID> idSet;
    public Set<UUID> idExcludeSet;
    public Set<Integer> featurerIdSet;
    public Set<Integer> featurerIdExcludeSet;
    public Ternary active;
    public Ternary logEnabled;
    public Set<String> cronSet;
    public Set<String> cronExcludeSet;
    public Set<Integer> fixedRateSet;
    public Set<Integer> fixedRateExcludeSet;
    public Set<String> descriptionSet;
    public Set<String> descriptionExcludeSet;
    public DataTimeRange createdAt;
    public DataTimeRange updatedAt;
}

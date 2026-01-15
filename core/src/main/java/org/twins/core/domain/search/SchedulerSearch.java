package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SchedulerSearch {
    public Set<UUID> idSet;
    public Set<UUID> idExcludeSet;
    public Set<UUID> domainIdSet;
    public Set<UUID> domainIdExcludeSet;
    public Set<Integer> featurerIdSet;
    public Set<Integer> featurerIdExcludeSet;
    public Ternary active;
    public Ternary logEnabled;
    public Set<String> cronSet;
    public Set<String> cronExcludeSet;
    public IntegerRangeDTOv1 fixedRateRange;
    public Set<String> descriptionLikeSet;
    public Set<String> descriptionNotLikeSet;
    public DataTimeRangeDTOv1 createdAtRange;
    public DataTimeRangeDTOv1 updatedAtRange;
}

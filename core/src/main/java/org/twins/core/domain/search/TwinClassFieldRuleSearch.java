package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFieldRuleSearch {

    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinClassFieldIdList;
    public Set<UUID> twinClassFieldIdExcludeList;
    public Set<Integer> fieldOverwriterFeaturerIdList;
    public Set<Integer> fieldOverwriterFeaturerIdExcludeList;
}

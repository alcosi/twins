package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class FeaturerSearch {
    private Set<Integer> idList;
    private Set<Integer> typeIdList;
    private Set<String> nameLikeList;
}

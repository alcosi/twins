package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DomainSearch {
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
}

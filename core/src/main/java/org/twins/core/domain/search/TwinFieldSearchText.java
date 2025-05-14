package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchText extends TwinFieldSearch {
    public Set<String> valueLikeAllOfList;
    public Set<String> valueLikeAnyOfList;
    public Set<String> valueLikeNoAllOfList;
    public Set<String> valueLikeNoAnyOfList;
}

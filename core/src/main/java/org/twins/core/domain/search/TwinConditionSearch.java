package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinConditionSearch {
    private Boolean matchAll;
    private List<BasicSearch> childTwins;
}

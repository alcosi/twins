package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BasicSearchList {
    private boolean matchAll = false;
    private List<BasicSearch> searches;
}

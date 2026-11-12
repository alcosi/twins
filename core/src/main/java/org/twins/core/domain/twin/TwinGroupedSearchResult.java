package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TwinGroupedSearchResult {
    private List<TwinGroup> groups = new ArrayList<>();
}

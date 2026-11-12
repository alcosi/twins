package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinGroupedSearchResult {
    private UUID groupFieldId;
    private TwinGroupKeyKind keyKind;
    private List<TwinGroup> groups = new ArrayList<>();
}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.SortDirection;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class SearchByAlias {
    String alias;
    BasicSearch narrow;
    Map<UUID, SortDirection> sortFields;
    Map<String, String> params;
}

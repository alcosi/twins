package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SearchByAlias {
    String alias;
    BasicSearch narrow;
    List<TwinSort> sorts;
    Map<String, String> params;
}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class SearchAlias extends TwinSearch {
    String alias;
    BasicSearch narrow;
    Map<String, String> params;
}

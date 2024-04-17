package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BasicSearch extends TwinSearch {
    TwinSearch headSearch;
}

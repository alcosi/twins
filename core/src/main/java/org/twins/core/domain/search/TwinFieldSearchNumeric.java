package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TwinFieldSearchNumeric extends TwinFieldSearch {

    String lessThen;
    String moreThen;
    String equals;

}

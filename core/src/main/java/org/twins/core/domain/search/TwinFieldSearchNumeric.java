package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TwinFieldSearchNumeric extends TwinFieldSearch {

    Double lessThen;
    Double moreThen;
    Double equals;

}

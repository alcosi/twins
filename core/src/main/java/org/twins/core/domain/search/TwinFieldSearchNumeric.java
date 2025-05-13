package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

@Data
@Accessors(chain = true)
public class TwinFieldSearchNumeric extends TwinFieldSearch {

    Double lessThen;
    Double moreThen;
    Double equals;

    public boolean isEmptySearch() {
        return ObjectUtils.isEmpty(lessThen) &&
                ObjectUtils.isEmpty(moreThen) &&
                ObjectUtils.isEmpty(equals);
    }

}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchBoolean extends TwinFieldSearch {

    private Boolean value;

    public boolean isEmptySearch() {
        return value == null;
    }
}

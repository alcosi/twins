package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldValueSearchNotImplemented extends TwinFieldValueSearch {
    @Override
    public boolean isEmptySearch() {
        return true;
    }
}


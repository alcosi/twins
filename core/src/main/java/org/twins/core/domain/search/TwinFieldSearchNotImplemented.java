package org.twins.core.domain.search;

import lombok.Data;

@Data
public class TwinFieldSearchNotImplemented extends TwinFieldSearch {
    public boolean isEmptySearch() {
        return true;
    }
}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinFieldSearchBaseUuid extends TwinFieldSearch {

    public Set<UUID> idList;
    public Set<UUID> idExcludeList;

}

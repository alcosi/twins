package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinFieldSearchList extends TwinFieldSearch {
    public Set<UUID> optionsAllOfList;
    public Set<UUID> optionsAnyOfList;
    public Set<UUID> optionsNoAllOfList;
    public Set<UUID> optionsNoAnyOfList;
}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinflowFactorySearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinflowIdList;
    private Set<UUID> twinflowIdExcludeList;
    private Set<UUID> twinFactoryIdList;
    private Set<UUID> twinFactoryIdExcludeList;
    private Set<String> factoryLauncherList;
    private Set<String> factoryLauncherExcludeList;
}

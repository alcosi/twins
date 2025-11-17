package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinflowFactorySearch {
    private Set<UUID> idSet;
    private Set<UUID> idExcludeSet;
    private Set<UUID> twinflowIdSet;
    private Set<UUID> twinflowIdExcludeSet;
    private Set<UUID> twinFactoryIdSet;
    private Set<UUID> twinFactoryIdExcludeSet;
    private Set<String> factoryLauncherSet;
    private Set<String> factoryLauncherExcludeSet;
}

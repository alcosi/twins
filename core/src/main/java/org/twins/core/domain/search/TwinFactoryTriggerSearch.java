package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinFactoryTriggerSearch {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinFactoryIdList;
    public Set<UUID> twinFactoryIdExcludeList;
    public Set<UUID> inputTwinClassIdList;
    public Set<UUID> inputTwinClassIdExcludeList;
    public Set<UUID> twinTriggerIdList;
    public Set<UUID> twinTriggerIdExcludeList;
    public Ternary active;
    public Ternary async;
}

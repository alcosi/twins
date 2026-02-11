package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassDynamicMarkerSearch {

    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Map<UUID, Boolean> twinClassIdMap;
    public Map<UUID, Boolean> twinClassIdExcludeMap;
    public Set<UUID> twinValidatorSetIdList;
    public Set<UUID> twinValidatorSetIdExcludeList;
    public Set<UUID> markerDataListOptionIdList;
    public Set<UUID> markerDataListOptionIdExcludeList;
}

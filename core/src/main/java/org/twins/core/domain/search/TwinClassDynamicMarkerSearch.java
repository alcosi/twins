package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassDynamicMarkerSearch {

    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<UUID> twinClassIdList;
    public Set<UUID> twinClassIdExcludeList;
    public Set<UUID> twinValidatorSetIdList;
    public Set<UUID> twinValidatorSetIdExcludeList;
    public Set<UUID> markerDataListOptionIdList;
    public Set<UUID> markerDataListOptionIdExcludeList;
}

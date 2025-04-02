package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class SpaceSearch {
    private UUID spaceId;
    private UUID roleId;
}

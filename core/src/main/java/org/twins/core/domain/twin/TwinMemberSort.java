package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.SortDirection;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinMemberSort {
    private UUID fieldId;
    private SortDirection direction;
}

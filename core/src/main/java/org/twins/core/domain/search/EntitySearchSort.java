package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class EntitySearchSort<S> {
    private DomainBusinessAccountUserSortField sortField = DomainBusinessAccountUserSortField.createdAt;
    private SortDirection sortDirection = SortDirection.ASC;
}

package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSortDTOv1;
import org.twins.core.enums.sort.TwinClassFieldSortField;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

@Component
public class TwinClassFieldSortDTOReverseMapper extends SortDTOReverseMapper<TwinClassFieldSortDTOv1, TwinClassFieldSortField> {
    @Override
    protected TwinClassFieldSortField sortField(TwinClassFieldSortDTOv1 dto) {
        return dto.getSortField();
    }
}

package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twinclass.TwinClassSortDTOv1;
import org.twins.core.enums.sort.TwinClassSortField;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

@Component
public class TwinClassSortDTOReverseMapper extends SortDTOReverseMapper<TwinClassSortDTOv1, TwinClassSortField> {
    @Override
    protected TwinClassSortField sortField(TwinClassSortDTOv1 dto) {
        return dto.getSortField();
    }
}

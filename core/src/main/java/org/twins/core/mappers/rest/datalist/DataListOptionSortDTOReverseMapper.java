package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.datalist.DataListOptionSortDTOv1;
import org.twins.core.enums.sort.DataListOptionSortField;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

@Component
public class DataListOptionSortDTOReverseMapper extends SortDTOReverseMapper<DataListOptionSortDTOv1, DataListOptionSortField> {
    @Override
    protected DataListOptionSortField sortField(DataListOptionSortDTOv1 dto) {
        return dto.getSortField();
    }
}

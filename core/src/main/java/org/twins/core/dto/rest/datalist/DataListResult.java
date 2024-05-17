package org.twins.core.dto.rest.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.service.pagination.PaginationResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataListResult extends PaginationResult {
    private List<DataListOptionEntity> dataOptionList;
}

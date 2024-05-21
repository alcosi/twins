package org.twins.core.dto.rest.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.service.pagination.PaginationResult;

@Data
@Accessors(chain = true)
public class DataListResult extends PaginationResult {
    private DataListDTOv1 dataList;
}

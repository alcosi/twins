package org.twins.core.dto.rest.datalist;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.service.pagination.PaginationResult;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class DataListOptionResult extends PaginationResult{
    private Kit<DataListOptionEntity, UUID> optionKit;
}

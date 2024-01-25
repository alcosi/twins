package org.twins.core.service.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinSearchResult extends PageableResult {
    private List<TwinEntity> response;
}

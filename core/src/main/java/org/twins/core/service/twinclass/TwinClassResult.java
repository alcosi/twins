package org.twins.core.service.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinClassResult extends PageableResult {
    public List<TwinClassEntity> twinClassList;
}

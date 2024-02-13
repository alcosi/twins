package org.twins.core.service.history;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.service.pagination.PageableResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class HistoryListResult extends PageableResult {
    private List<HistoryEntity> historyList;
}

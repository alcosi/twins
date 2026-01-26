package org.twins.core.domain.history;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.dao.twin.TwinEntity;

import java.time.Instant;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinHistoryItem {
    private UUID id;
    private TwinEntity twin;
    private HistoryType type;
    private Instant createdAt;

}

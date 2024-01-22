package org.twins.core.service.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinHistoryItem;
import org.twins.core.dto.rest.history.HistoryType;
import org.twins.core.service.twin.TwinService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryService {
    final TwinService twinService;
    public List<TwinHistoryItem> findHistory(UUID twinId, int childDepth) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        List<TwinHistoryItem> ret = new ArrayList<>();
        ret.add(new TwinHistoryItem()
                .setId(UUID.randomUUID())
                .setTwin(twinEntity)
                .setType(HistoryType.twinCreated)
                .setCreatedAt(Instant.now().minusSeconds(600)));
        ret.add(new TwinHistoryItem()
                .setId(UUID.randomUUID())
                .setTwin(twinEntity)
                .setType(HistoryType.statusChanged)
                .setCreatedAt(Instant.now().minusSeconds(900)));
        ret.add(new TwinHistoryItem()
                .setId(UUID.randomUUID())
                .setTwin(twinEntity)
                .setType(HistoryType.assigneeChanged)
                .setCreatedAt(Instant.now().minusSeconds(1200)));
        ret.add(new TwinHistoryItem()
                .setId(UUID.randomUUID())
                .setTwin(twinEntity)
                .setType(HistoryType.nameChanged)
                .setCreatedAt(Instant.now().minusSeconds(1500)));
        return ret; //todo
    }
}

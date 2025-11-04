package org.twins.core.service.history;

import org.twins.core.dao.history.HistoryRepository;

import java.util.UUID;

/**
 * Lightweight DTO used by HistoryService and scheduling components when dispatching notifications.
 * It contains the twin identifier, domain it belongs to, the user IDs that should be notified and
 * the history item IDs that must be marked as dispatched.
 */
public record TwinUsersForDispatch(
        UUID twinId,
        UUID domainId,
        UUID[] userIds,
        UUID[] historyIds
) implements HistoryRepository.TwinUsersProjection {

    @Override
    public UUID getTwinId() {
        return twinId;
    }

    @Override
    public UUID getDomainId() {
        return domainId;
    }

    @Override
    public UUID[] getUserIds() {
        return userIds;
    }

    @Override
    public UUID[] getHistoryIds() {
        return historyIds;
    }
}

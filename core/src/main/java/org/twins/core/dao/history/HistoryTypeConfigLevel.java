package org.twins.core.dao.history;

import java.util.UUID;

public interface HistoryTypeConfigLevel {
    HistoryTypeStatus getStatus();
    String getSnapshotMessageTemplate();
    UUID getMessageTemplateI18nId();
}

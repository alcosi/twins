package org.twins.core.service.history;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.HistoryTypeStatus;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryTypeConfig {
    private HistoryTypeStatus status = HistoryTypeStatus.softDisabled; //by default
    private String snapshotMessageTemplate;
    private UUID messageTemplateI18nId;
}

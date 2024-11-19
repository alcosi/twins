package org.twins.core.dao.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "history_type")
public class HistoryTypeEntity implements EasyLoggable, HistoryTypeConfigLevel {
    @Id
    private String id;

    @Column(name = "history_type_status_id")
    @Convert(converter = HistoryTypeStatusConverter.class)
    private HistoryTypeStatus status;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Override
    public String easyLog(Level level) {
        return "historyType[" + id + "]";
    }

    @Override
    public UUID getMessageTemplateI18nId() {
        return null; //templates can not be configured on global level
    }
}

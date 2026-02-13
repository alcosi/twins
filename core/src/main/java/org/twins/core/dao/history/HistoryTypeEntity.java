package org.twins.core.dao.history;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "history_type")
public class HistoryTypeEntity implements EasyLoggable {
    @Id
    // @Convert(converter = HistoryTypeConverter.class)
    private String id;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Override
    public String easyLog(Level level) {
        return "historyType[" + id + "]";
    }
}

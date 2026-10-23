package org.twins.core.dao.history;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.datascope.DataScopeType;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "history_type")
@DataScope(DataScopeType.SYSTEM)
public class HistoryTypeEntity implements EasyLoggable, Identifiable<String> {
    @Id
//    @Convert(converter = HistoryTypeConverter.class)
    private String id;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Override
    public String easyLog(Level level) {
        return "historyType[" + id + "]";
    }
}

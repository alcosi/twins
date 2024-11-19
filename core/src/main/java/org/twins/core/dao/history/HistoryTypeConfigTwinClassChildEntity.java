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
@Table(name = "history_type_config_twin_class_child")
public class HistoryTypeConfigTwinClassChildEntity implements EasyLoggable, HistoryTypeConfigLevel {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "child_twin_class_id")
    private UUID childTwinClassId;

    @Column(name = "child_history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType childHistoryType;

    @Column(name = "history_type_status_id")
    @Convert(converter = HistoryTypeStatusConverter.class)
    private HistoryTypeStatus status;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Column(name = "message_template_i18n_id")
    private UUID messageTemplateI18nId;

//    @ManyToOne
//    @JoinColumn(name = "history_type_id", insertable = false, updatable = false, nullable = false)
//    private HistoryTypeEntity historyTypeEntity;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "historyTypeConfigTwinClassChild[" + id + "]";
            default:
                return "historyTypeConfigTwinClassChild[id:" + id + ", twinClassId:" + twinClassId + ", childTwinClassId:" + childTwinClassId + ", childHistoryType:" + childHistoryType +  ", status:" + status + "]";
        }
    }
}

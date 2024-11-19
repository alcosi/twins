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
@Table(name = "history_type_config_domain")
public class HistoryTypeConfigDomainEntity implements EasyLoggable, HistoryTypeConfigLevel {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "history_type_status_id")
    @Convert(converter = HistoryTypeStatusConverter.class)
    private HistoryTypeStatus status;

    @Column(name = "domain_id")
    private UUID domainId;

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
                return "historyTypeConfigDomain[" + id + "]";
            default:
                return "historyTypeConfigDomain[id:" + id + ", historyType:" + historyType + ", domainId:" + domainId + ", status:" + status + "]";
        }
    }
}

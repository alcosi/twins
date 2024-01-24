package org.twins.core.dao.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggableImpl;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "history_type_domain_template")
public class HistoryTypeDomainTemplateEntity extends EasyLoggableImpl {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @ManyToOne
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false, nullable = false)
    private HistoryTypeEntity historyTypeEntity;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "historyTypeDomainTemplate[" + id + "]";
            default:
                return "historyTypeDomainTemplate[id:" + id + ", domainId:" + domainId + "]";
        }
    }
}

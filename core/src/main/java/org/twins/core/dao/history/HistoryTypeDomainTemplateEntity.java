package org.twins.core.dao.history;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.enums.history.HistoryType;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "history_type_domain_template")
public class HistoryTypeDomainTemplateEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false, nullable = false)
    private HistoryTypeEntity historyTypeEntity;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "historyTypeDomainTemplate[" + id + "]";
            default -> "historyTypeDomainTemplate[id:" + id + ", domainId:" + domainId + "]";
        };
    }
}

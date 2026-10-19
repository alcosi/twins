package org.twins.core.dao.history;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
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
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "history_type_id")
    @Convert(converter = HistoryTypeConverter.class)
    private HistoryType historyType;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_type_id", insertable = false, updatable = false, nullable = false)
    private HistoryTypeEntity historyTypeEntitySpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private HistoryTypeEntity historyTypeEntity;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "historyTypeDomainTemplate[" + id + "]";
            default -> "historyTypeDomainTemplate[id:" + id + ", domainId:" + domainId + "]";
        };
    }
}

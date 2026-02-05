package org.twins.core.dao.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Entity
@DomainSetting
@Data
@Accessors(chain = true)
@Table(name = "history_type")
public class HistoryTypeEntity implements EasyLoggable {
    @Id
    // @Convert(converter = HistoryTypeConverter.class)
    private String id;

    @Column(name = "snapshot_message_template")
    private String snapshotMessageTemplate;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        return "historyType[" + id + "]";
    }
}

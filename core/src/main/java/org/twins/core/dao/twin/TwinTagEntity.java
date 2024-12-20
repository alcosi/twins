package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"twin_id", "tag_data_list_option_id"}, name = "idx_twin_tag_unique")
})
public class TwinTagEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "tag_data_list_option_id")
    private UUID tagDataListOptionId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "tag_data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity tagDataListOption;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinTag[" + id + "]";
            default -> "twinTag[id:" + id + ", twinId:" + twinId + "]";
        };
    }
}

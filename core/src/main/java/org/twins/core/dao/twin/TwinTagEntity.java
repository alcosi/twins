package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "twin_tag", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"twin_id", "tag_data_list_option_id"}, name = "idx_twin_tag_unique")
})
public class TwinTagEntity extends EasyLoggableImpl {
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
        switch (level) {
            case SHORT:
                return "twinTag[" + id + "]";
            default:
                return "twinTag[id:" + id + ", twinId:" + twinId + "]";
        }
    }
}

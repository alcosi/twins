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
@Table(name = "twin_marker", uniqueConstraints = {@UniqueConstraint(columnNames = {"twin_id", "marker_data_list_option_id"}, name = "idx_twin_marker_unique")})
public class TwinMarkerEntity extends EasyLoggableImpl {
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

    @Column(name = "marker_data_list_option_id")
    private UUID markerDataListOptionId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "marker_data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity markerDataListOption;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinMarker[" + id + "]";
            default:
                return "twinMarker[id:" + id + ", twinId:" + twinId + "]";
        }
    }
}

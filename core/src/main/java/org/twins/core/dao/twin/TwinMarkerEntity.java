package org.twins.core.dao.twin;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_marker", uniqueConstraints = {@UniqueConstraint(columnNames = {"twin_id", "marker_data_list_option_id"}, name = "idx_twin_marker_unique")})
@FieldNameConstants
public class TwinMarkerEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "marker_data_list_option_id")
    private UUID markerDataListOptionId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "marker_data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity markerDataListOption;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinMarker[" + id + "]";
            default -> "twinMarker[id:" + id + ", twinId:" + twinId + "]";
        };
    }
}

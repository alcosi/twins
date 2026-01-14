package org.twins.core.dao.datalist;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "data_list_option_projection")
public class DataListOptionProjectionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "projection_type_id")
    private UUID projectionTypeId;

    @Column(name = "src_data_list_option_id")
    private UUID srcDataListOptionId;

    @Column(name = "dst_data_list_option_id")
    private UUID dstDataListOptionId;

    @Column(name = "saved_by_user_id")
    private UUID savedByUserId;

    @Column(name = "changed_at")
    private Timestamp changedAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projection_type_id", insertable = false, updatable = false)
    private ProjectionTypeEntity ProjectionType;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity srcDataListOption;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity dstDataListOption;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_by_user_id", insertable = false, updatable = false)
    private UserEntity savedByUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "dataListOptionProjection[" + id + "]";
            case NORMAL -> "dataListOptionProjection[id:" + id + ", projectionType:" + projectionTypeId + "]";
            default -> "dataListOptionProjection[id:" + id + ", projectionType:" + projectionTypeId + ", srcOpt:" + srcDataListOptionId + ", dstOpt:" + dstDataListOptionId + "]";
        };
    }
}

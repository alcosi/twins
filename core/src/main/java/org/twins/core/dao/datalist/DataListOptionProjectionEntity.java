package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
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

    @Column(name = "data_list_projection_id")
    private UUID dataListProjectionId;

    @Column(name = "src_data_list_option_id")
    private UUID srcDataListOptionId;

    @Column(name = "dst_data_list_option_id")
    private UUID dstDataListOptionId;

    @Column(name = "saved_by_user_id")
    private UUID savedByUserId;

    @Column(name = "saved_at")
    private Timestamp savedAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_projection_id", insertable = false, updatable = false)
    private DataListProjectionEntity dataListProjection;

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
            case NORMAL -> "dataListOptionProjection[id:" + id + ", proj:" + dataListProjectionId + "]";
            default -> "dataListOptionProjection[id:" + id + ", proj:" + dataListProjectionId + ", srcOpt:" + srcDataListOptionId + ", dstOpt:" + dstDataListOptionId + "]";
        };
    }
}

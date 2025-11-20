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
@Table(name = "data_list_projection")
public class DataListProjectionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @Column(name = "src_data_list_id")
    private UUID srcDataListId;

    @Column(name = "dst_data_list_id")
    private UUID dstDataListId;

    @Column(name = "name")
    private String name;

    @Column(name = "saved_by_user_id")
    private UUID savedByUserId;

    @Column(name = "changed_at")
    private Timestamp changedAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_data_list_id", insertable = false, updatable = false)
    private DataListEntity srcDataList;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_data_list_id", insertable = false, updatable = false)
    private DataListEntity dstDataList;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_by_user_id", insertable = false, updatable = false)
    private UserEntity savedByUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "dataListProjection[" + id + "]";
            case NORMAL -> "dataListProjection[id:" + id + ", src:" + srcDataListId + ", dst:" + dstDataListId + "]";
            default -> "dataListProjection[id:" + id + ", src:" + srcDataListId + ", dst:" + dstDataListId + ", name:" + name + "]";
        };
    }
}

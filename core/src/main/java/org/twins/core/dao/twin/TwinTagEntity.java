package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
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
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "tag_data_list_option_id")
    private UUID tagDataListOptionId;

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

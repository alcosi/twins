package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_data_list")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldDataListEntity extends TwinFieldBaseEntity {
    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity dataListOption;

    @Override
    public TwinFieldDataListEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldDataListEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldDataListEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldDataListEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldDataListEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return "twinFieldDataList[id:" + getId() + "]";
    }

    public TwinFieldDataListEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldDataListEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setDataListOption(dataListOption)
                .setDataListOptionId(dataListOptionId);
    }
}

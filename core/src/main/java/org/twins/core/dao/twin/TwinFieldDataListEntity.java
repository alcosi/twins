package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_data_list")
@FieldNameConstants
public class TwinFieldDataListEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity dataListOption;

    @Override
    public String easyLog(Level level) {
        return "twinFieldDataList[id:" + id + "]";
    }

    public TwinFieldDataListEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldDataListEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassFieldId(twinClassFieldId)
                .setDataListOption(dataListOption)
                .setDataListOptionId(dataListOptionId);
    }
}

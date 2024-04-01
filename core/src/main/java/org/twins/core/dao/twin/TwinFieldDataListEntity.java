package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_data_list")
public class TwinFieldDataListEntity implements EasyLoggable, TwinFieldStorage {
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

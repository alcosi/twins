package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_data_list")
public class TwinFieldDataListEntity implements EasyLoggable, TwinFieldStorage {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_field_id")
    private UUID twinFieldId;

    @Column(name = "data_list_option_id")
    private UUID dataListOptionId;

    @ManyToOne
    @JoinColumn(name = "twin_field_id", insertable = false, updatable = false, nullable = false)
    private TwinFieldEntity twinField;

    @ManyToOne
    @JoinColumn(name = "data_list_option_id", insertable = false, updatable = false, nullable = false)
    private DataListOptionEntity dataListOption;

    @Override
    public String easyLog(Level level) {
        return "twinFieldDataList[id:" + id + "]";
    }
}

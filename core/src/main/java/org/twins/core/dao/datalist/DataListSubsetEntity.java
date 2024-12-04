package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_subset")
@FieldNameConstants
public class DataListSubsetEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "key")
    private String key;

    @ManyToOne
    @JoinColumn(name = "data_list_id", insertable = false, updatable = false)
    private DataListEntity dataList;

    public String easyLog(Level level) {
        return "dataList[id:" + id + ", key:" + key + "]";
    }

}

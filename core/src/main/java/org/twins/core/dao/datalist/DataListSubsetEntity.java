package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_subset")
@FieldNameConstants
public class DataListSubsetEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "key")
    private String key;

    //needed for specification
    @Deprecated
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "dataListSubset", fetch = FetchType.LAZY)
    private Set<DataListSubsetOptionEntity> subsetOptions;

    public String easyLog(Level level) {
        return "dataList[id:" + id + ", key:" + key + "]";
    }
}

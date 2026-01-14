package org.twins.core.dao.datalist;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.UuidGenerator;

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
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
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

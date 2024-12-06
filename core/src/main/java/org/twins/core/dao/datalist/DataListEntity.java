package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinTagEntity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list")
@FieldNameConstants
public class DataListEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "attribute_1_key")
    private String attribute1key;

    @Column(name = "attribute_2_key")
    private String attribute2key;

    @Column(name = "attribute_3_key")
    private String attribute3key;

    @Column(name = "attribute_4_key")
    private String attribute4key;

    public String easyLog(Level level) {
        return "dataList[id:" + id + ", key:" + key + "]";
    }

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<DataListOptionEntity, UUID> options;

    //needed for specification
    @Deprecated
    @OneToMany(mappedBy = "dataList")
    @EqualsAndHashCode.Exclude
    private Collection<DataListOptionEntity> dataListOptions;
}

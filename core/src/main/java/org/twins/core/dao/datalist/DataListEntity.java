package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list")
public class DataListEntity {
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

    @Transient
    List<DataListOptionEntity> options;
}

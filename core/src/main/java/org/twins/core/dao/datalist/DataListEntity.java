package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "data_list")
public class DataListEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}

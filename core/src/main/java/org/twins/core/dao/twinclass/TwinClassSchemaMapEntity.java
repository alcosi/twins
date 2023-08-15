package org.twins.core.dao.twinclass;

import lombok.Data;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_schema_map")
public class TwinClassSchemaMapEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @ManyToOne
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false, nullable = false)
    private TwinClassSchemaEntity twinClassSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;
}

package org.twins.core.dao.twinflow;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "twinflow_schema_map")
public class TwinflowSchemaMapEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twinflow_id")
    private UUID twinflowId;

    @ManyToOne
    @JoinColumn(name = "twinflow_schema_id", insertable = false, updatable = false)
    private TwinflowSchemaEntity twinflowSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @JoinColumn(name = "twinflow_id", insertable = false, updatable = false)
    private TwinflowEntity twinflow;
}

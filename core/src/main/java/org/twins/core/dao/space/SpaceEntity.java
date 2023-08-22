package org.twins.core.dao.space;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "space")
public class SpaceEntity {
    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "key")
    private String key;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "alias_counter")
    private Integer aliasCounter;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "twinflow_schema_id", insertable = false, updatable = false)
    private TwinflowSchemaEntity twinflowSchema;

    @ManyToOne
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false)
    private TwinClassSchemaEntity twinClassSchema;
}

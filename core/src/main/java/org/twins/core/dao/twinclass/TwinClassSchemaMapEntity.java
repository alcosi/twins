package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.*;
import org.cambium.common.util.UuidUtils;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_schema_map")
public class TwinClassSchemaMapEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twins_count_quota")
    private Integer twinsCountQuota;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false, nullable = false)
    private TwinClassSchemaEntity twinClassSchemaSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassSchemaEntity twinClassSchema;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;
}

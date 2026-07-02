package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@MappedSuperclass
@Data
@Accessors(chain = true)
@FieldNameConstants
public abstract class TwinFieldBaseEntity implements EasyLoggable, Identifiable {
    @Id
    protected UUID id;

    @Column(name = "twin_id")
    protected UUID twinId;

    @Column(name = "twin_class_field_id")
    protected UUID twinClassFieldId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twinSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassFieldSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    protected TwinEntity twin;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    protected TwinClassFieldEntity twinClassField;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }
}

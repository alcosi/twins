package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_twin_class")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldTwinClassEntity extends TwinFieldBaseEntity {

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @Override
    public TwinFieldTwinClassEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldTwinClassEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldTwinClassEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldTwinClassEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldTwinClassEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return "twinFieldTwinClass[id:" + getId() + "]";
    }

    public TwinFieldTwinClassEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldTwinClassEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setTwinClass(twinClass);
    }
}

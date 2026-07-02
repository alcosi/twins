package org.twins.core.dao.twin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_simple")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class TwinFieldSimpleEntity extends TwinFieldBaseEntity {
    @Column(name = "value")
    private String value;

    @Override
    public TwinFieldSimpleEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldSimpleEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldSimpleEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldSimpleEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldSimpleEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String toString() {
        return logDetailed();
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinField[" + getId() + "]";
            case NORMAL ->
                    "twinField[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + "]";
            default ->
                    "twinField[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + ", value:" + value + "]";
        };
    }

    public TwinFieldSimpleEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldSimpleEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setValue(value);
    }
}

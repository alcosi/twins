package org.twins.core.dao.twin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_boolean")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldBooleanEntity extends TwinFieldBaseEntity {
    @Column(name = "value")
    private Boolean value;

    @Override
    public TwinFieldBooleanEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldBooleanEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldBooleanEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldBooleanEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldBooleanEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinFieldBoolean[" + getId() + "]";
            case NORMAL ->
                    "twinFieldBoolean[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + "]";
            default ->
                    "twinFieldBoolean[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + ", value:" + value + "]";
        };
    }

    public static TwinFieldBooleanEntity of(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) {
        return new TwinFieldBooleanEntity()
                .setId(UuidUtils.generate())
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(null);
    }

    public TwinFieldBooleanEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldBooleanEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setValue(value);
    }
}

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
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_timestamp")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldTimestampEntity extends TwinFieldBaseEntity {
    @Column(name = "value")
    private Timestamp value;

    @Override
    public TwinFieldTimestampEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldTimestampEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldTimestampEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldTimestampEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldTimestampEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinFieldTimestamp[" + getId() + "]";
            case NORMAL ->
                    "twinFieldTimestamp[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + "]";
            default ->
                    "twinFieldTimestamp[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + ", value:" + value + "]";
        };
    }

    public TwinFieldTimestampEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldTimestampEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setValue(value);
    }
}

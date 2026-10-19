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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_decimal")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldDecimalEntity extends TwinFieldBaseEntity {

    @Column(name = "value")
    private BigDecimal value;

    @Override
    public TwinFieldDecimalEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldDecimalEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldDecimalEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldDecimalEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldDecimalEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinFieldDecimalEntity[" + getId() + "]";
            case NORMAL ->
                    "twinFieldDecimalEntity[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + "]";
            default ->
                    "twinFieldDecimalEntity[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + ", value:" + value + "]";
        };
    }

    public static TwinFieldDecimalEntity of(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) {
        return new TwinFieldDecimalEntity()
                .setId(UuidUtils.generate())
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(null);
    }

    public TwinFieldDecimalEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldDecimalEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setValue(value);
    }
}

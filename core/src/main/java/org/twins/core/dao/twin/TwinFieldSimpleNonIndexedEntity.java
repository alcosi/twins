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

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_simple_non_indexed")
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldSimpleNonIndexedEntity extends TwinFieldBaseEntity {

    @Column(name = "value")
    private String value;

    @Override
    public TwinFieldSimpleNonIndexedEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldSimpleNonIndexedEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldSimpleNonIndexedEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldSimpleNonIndexedEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldSimpleNonIndexedEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinFieldNonIndexed[" + getId() + "]";
            case NORMAL ->
                    "twinFieldNonIndexed[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + "]";
            default ->
                    "twinFieldNonIndexed[id:" + getId() + (getTwinClassField() != null ? ", key:" + getTwinClassField().getKey() : "") + ", value:" + value + "]";
        };
    }

    public TwinFieldSimpleNonIndexedEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldSimpleNonIndexedEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(getTwinClassField())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setValue(value);
    }
}

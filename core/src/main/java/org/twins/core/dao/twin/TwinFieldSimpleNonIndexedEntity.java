package org.twins.core.dao.twin;

import jakarta.persistence.*;
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
public class TwinFieldSimpleNonIndexedEntity implements EasyLoggable {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @Column(name = "value")
    private String value;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFieldNonIndexed[" + id + "]";
            case NORMAL -> "twinFieldNonIndexed[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + "]";
            default -> "twinFieldNonIndexed[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + ", value:" + value + "]";
        };
    }

    public TwinFieldSimpleNonIndexedEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldSimpleNonIndexedEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassFieldId)
                .setValue(value);
    }
}

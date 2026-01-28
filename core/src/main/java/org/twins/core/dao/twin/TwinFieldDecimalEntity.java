package org.twins.core.dao.twin;

import jakarta.persistence.*;
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
public class TwinFieldDecimalEntity implements EasyLoggable {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
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
    private BigDecimal value;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFieldDecimalEntity[" + id + "]";
            case NORMAL ->
                    "twinFieldDecimalEntity[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + "]";
            default ->
                    "twinFieldDecimalEntity[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + ", value:" + value + "]";
        };
    }

    public TwinFieldDecimalEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldDecimalEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassFieldId)
                .setValue(value);
    }
}


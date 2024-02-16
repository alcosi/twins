package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "twin_field")
@FieldNameConstants
public class TwinFieldEntity extends EasyLoggableImpl {
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

    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @Override
    public String toString() {
        return logDetailed();
    }

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinField[" + id + "]";
            case NORMAL:
                return "twinField[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + "]";
            default:
                return "twinField[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + ", value:" + value + "]";
        }
    }
}

package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field")
public class TwinFieldEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

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
    public String easyLog(Level level) {
        return "twinField[id:" + id + (twinClassField != null ? ", key:" + twinClassField.getKey() : "") + ", value:" + value + "]";
    }
}

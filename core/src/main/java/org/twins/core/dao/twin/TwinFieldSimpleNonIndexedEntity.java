package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_simple_non_indexed")
public class TwinFieldSimpleNonIndexedEntity implements TwinFieldStorage, EasyLoggable {

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

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

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
}

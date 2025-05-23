package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.Arrays;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_boolean")
public class TwinFieldBooleanEntity implements TwinFieldStorage {
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
    private boolean value;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    public TwinFieldBooleanEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldBooleanEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassFieldId)
                .setValue(value);
    }

    @Getter
    public enum CheckboxType {
        STANDARD("STANDARD"),
        TOGGLE("TOGGLE"),
        CUSTOM("CUSTOM");

        private final String id;

        CheckboxType(String id) {
            this.id = id;
        }

        public static TwinFieldBooleanEntity.CheckboxType valueOfId(String type) {
            return Arrays.stream(TwinFieldBooleanEntity.CheckboxType.values()).filter(c -> c.id.equalsIgnoreCase(type)).findAny().orElseThrow();
        }
    }

}

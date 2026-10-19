package org.twins.core.dao.twin;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassFieldAttributeEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_attribute")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldAttributeEntity extends TwinFieldBaseEntity
        implements PublicCloneable<TwinFieldAttributeEntity> {

    @Column(name = "twin_class_field_attribute_id")
    private UUID twinClassFieldAttributeId;

    @Column(name = "note_msg")
    private String noteMsg;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "note_msg_context", columnDefinition = "hstore")
    private HashMap<String, String> noteMsgContext;

    @Column(name = "changed_at")
    private Timestamp changedAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_field_attribute_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldAttributeEntity twinClassFieldAttribute;

    @Override
    public TwinFieldAttributeEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldAttributeEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldAttributeEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldAttributeEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldAttributeEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return "twinFieldAttribute[id:" + getId() + "]";
    }

    @Override
    public TwinFieldAttributeEntity clone() {
        return new TwinFieldAttributeEntity()
                .setId(getId())
                .setTwinId(getTwinId())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setTwinClassFieldAttributeId(twinClassFieldAttributeId)
                .setNoteMsg(noteMsg)
                .setNoteMsgContext(noteMsgContext)
                .setChangedAt(changedAt)
                .setTwinClassFieldAttribute(twinClassFieldAttribute)
                .setTwin(getTwin());
    }
}

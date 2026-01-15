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
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassFieldAttributeEntity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_attribute")
public class TwinFieldAttributeEntity implements EasyLoggable, PublicCloneable<TwinFieldAttributeEntity> {
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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @Override
    public String easyLog(Level level) {
        return "twinFieldAttribute[id:" + id + "]";
    }

    @Override
    public TwinFieldAttributeEntity clone() {
        return new TwinFieldAttributeEntity()
                .setId(id)
                .setTwinId(twinId)
                .setTwinClassFieldId(twinClassFieldId)
                .setTwinClassFieldAttributeId(twinClassFieldAttributeId)
                .setNoteMsg(noteMsg)
                .setNoteMsgContext(noteMsgContext)
                .setChangedAt(changedAt)
                .setTwinClassFieldAttribute(twinClassFieldAttribute)
                .setTwin(twin);
    }
}

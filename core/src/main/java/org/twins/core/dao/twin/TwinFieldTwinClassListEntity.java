package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_twin_class_list")
public class TwinFieldTwinClassListEntity implements EasyLoggable {

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

    @ManyToMany
    @JoinTable(
            name = "twin_class_list",
            joinColumns = @JoinColumn(name = "twin_field_twin_class_list_id"),
            inverseJoinColumns = @JoinColumn(name = "twin_class_id"))
    private Set<TwinClassEntity> twinClassSet;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return "twinFieldTwinClass[id:" + id + "]";
    }

    public TwinFieldTwinClassListEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldTwinClassListEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassFieldId)
                .setTwinClassSet(twinClassSet);
    }
}

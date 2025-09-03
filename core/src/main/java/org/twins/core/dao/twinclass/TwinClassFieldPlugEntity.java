package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_class_field_plug")
public class TwinClassFieldPlugEntity implements EasyLoggable {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return "twinClassFieldPlug[twinClassId: " + twinClassId + " , twinClassFieldId: " + twinClassFieldId +  "]";
    }
}

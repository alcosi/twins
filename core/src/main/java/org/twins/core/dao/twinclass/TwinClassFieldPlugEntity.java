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

    @EmbeddedId
    private TwinClassFieldPlugId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("twinClassId")
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("twinClassFieldId")
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFieldEntity twinClassField;

    @Data
    @Embeddable
    public static class TwinClassFieldPlugId {
        @Column(name = "twin_class_id")
        private UUID twinClassId;

        @Column(name = "twin_class_field_id")
        private UUID twinClassFieldId;
    }

    public UUID getTwinClassId() {
        return id.twinClassId;
    }

    public UUID getTwinClassFieldId() {
        return id.twinClassFieldId;
    }

    @Override
    public String easyLog(Level level) {
        return "twinClassFieldPlug[twinClassId: " + id.twinClassId + " , twinClassFieldId: " + id.twinClassFieldId +  "]";
    }
}

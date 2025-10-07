package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;


@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_availability")
@FieldNameConstants
public class TwinClassAvailabilityEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassAvailability[" + key + "]";
            default -> "twinClassAvailability[id:" + id + ", key:" + key + "]";
        };

    }
}

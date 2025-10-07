package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.i18n.I18nEntity;

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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity descriptionI18n;



    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassAvailability[" + key + "]";
            default -> "twinClassAvailability[id:" + id + ", key:" + key + "]";
        };

    }
}

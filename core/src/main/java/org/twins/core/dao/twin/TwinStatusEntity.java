package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_status")
@FieldNameConstants
public class TwinStatusEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twins_class_id") //todo rename to twin_class_id
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "logo")
    private String logo;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "font_color")
    private String fontColor;

    @ManyToOne
    @JoinColumn(name = "twins_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinStatus[" + id + "]";
            case NORMAL -> "twinStatus[id:" + id + ", key:" + key + "]";
            default -> "twinStatus[id:" + id + ", twinClassId:" + twinClassId + "]";
        };
    }
}

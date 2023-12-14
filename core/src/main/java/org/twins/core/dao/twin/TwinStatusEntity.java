package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggableImpl;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_status")
public class TwinStatusEntity extends EasyLoggableImpl {
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

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "logo")
    private String logo;

    @Column(name = "color")
    private String color;

    @ManyToOne
    @JoinColumn(name = "twins_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twinStatus[" + id + "]";
            default:
                return "twinStatus[id:" + id + ", twinClassId:" + twinClassId +  "]";
        }
    }

//    @ManyToOne
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;
//
//    @ManyToOne
//    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
//    private I18nEntity descriptionI18n;
}

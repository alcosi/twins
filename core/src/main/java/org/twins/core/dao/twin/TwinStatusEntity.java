package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_status")
public class TwinStatusEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twins_class_id")
    private UUID twinsClassId;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "logo")
    private String logo;

    @ManyToOne
    @JoinColumn(name = "twins_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassByTwinsClassId;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;
}

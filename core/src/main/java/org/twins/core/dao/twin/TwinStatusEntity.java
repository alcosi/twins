package org.twins.core.dao.twin;

import lombok.Data;

import jakarta.persistence.*;
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
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @ManyToOne
    @JoinColumn(name = "twins_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassByTwinsClassId;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByNameI18NId;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByDescriptionI18NId;
}

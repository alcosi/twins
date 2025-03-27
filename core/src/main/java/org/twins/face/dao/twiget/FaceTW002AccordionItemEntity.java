package org.twins.face.dao.twiget;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.LocaleConverter;

import java.util.Locale;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_twidget_TW002_accordion_item")
public class FaceTW002AccordionItemEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;
}
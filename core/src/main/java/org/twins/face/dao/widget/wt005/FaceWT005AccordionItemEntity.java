package org.twins.face.dao.widget.wt005;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.LocaleConverter;

import java.util.Locale;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt005_accordion_item")
public class FaceWT005AccordionItemEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt005_id")
    private UUID faceWT005Id;

    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_wt005_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT005Entity faceWT005;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity labelI18n;
}
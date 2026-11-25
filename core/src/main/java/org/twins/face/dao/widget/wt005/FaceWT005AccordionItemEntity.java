package org.twins.face.dao.widget.wt005;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.i18n.LocaleConverter;
import org.twins.core.domain.Identifiable;

import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt005_accordion_item")
public class FaceWT005AccordionItemEntity implements EasyLoggable, Identifiable<UUID> {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt005_id")
    private UUID faceWT005Id;

    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_wt005_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT005Entity faceWT005SpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT005Entity faceWT005;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT005AccordionItem[" + id + "]";
            default:
                return "faceWT005AccordionItem[id:" + id + ", faceWT005Id:" + faceWT005Id + ", locale:" + locale + "]";
        }
    }
}

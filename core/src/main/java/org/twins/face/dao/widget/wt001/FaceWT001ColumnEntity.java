package org.twins.face.dao.widget.wt001;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt001_column")
public class FaceWT001ColumnEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", nullable = false, insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;
}

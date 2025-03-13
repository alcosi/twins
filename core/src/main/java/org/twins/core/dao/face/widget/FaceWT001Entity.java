package org.twins.core.dao.face.widget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.face.FaceEntity;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt001")
public class FaceWT001Entity {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @Column(name = "key", nullable = false)
    private String key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity twinClass;

    @Column(name = "hide_columns")
    private List<String> hideColumns;
}
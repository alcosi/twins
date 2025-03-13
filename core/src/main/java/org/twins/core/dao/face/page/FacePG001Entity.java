package org.twins.core.dao.face.page;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.twins.core.dao.face.FaceEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_page_pg001")
public class FacePG001Entity {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @Basic
    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ColumnDefault("0")
    @Column(name = "widget_order", nullable = false)
    private Integer widgetOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity widgetFace;
}
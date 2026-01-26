package org.twins.face.dao.bc;

import jakarta.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerEntity;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_bc001_item")
public class FaceBC001ItemEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "`order`")
    @Basic
    private int order;

    @Column(name = "twin_pointer_id")
    private UUID twinPointerId;

    @Column(name = "face_bc001_id")
    private UUID faceBC001Id;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "label_id")
    private UUID labelId;

    @ManyToOne
    @JoinColumn(name = "twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity twinPointer;

    @ManyToOne
    @JoinColumn(name = "face_bc001_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceBC001Entity faceBC001;

    @ManyToOne
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @ManyToOne
    @JoinColumn(name = "label_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity label;
}

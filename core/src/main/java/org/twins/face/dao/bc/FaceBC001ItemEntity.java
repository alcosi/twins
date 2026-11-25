package org.twins.face.dao.bc;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_bc001_item")
public class FaceBC001ItemEntity implements EasyLoggable, Identifiable<UUID> {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "twin_pointer_id")
    private UUID twinPointerId;

    @Column(name = "face_bc001_id")
    private UUID faceBC001Id;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "label_id")
    private UUID labelId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity twinPointerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity twinPointer;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_bc001_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceBC001Entity faceBC001SpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResourceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceBC001Entity faceBC001;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceBC001Item[" + id + "]";
            default:
                return "faceBC001Item[id:" + id + ", faceBC001Id:" + faceBC001Id + ", order:" + order + "]";
        }
    }
}

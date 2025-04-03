package org.twins.core.dao.face;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.pointer.Pointer;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_twidget")
public class FaceTwidgetEntity implements EasyLoggable {
    @Id
    @Column(name = "face_id")
    private UUID faceId;

    @FeaturerList(type = Pointer.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pointer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity pointerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "pointer_params", columnDefinition = "hstore")
    private HashMap<String, String> pointerParams;


    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTwidget[" + faceId + "]";
            default:
                return "faceTwidget[id:" + faceId + ", pointerFeaturer:" + pointerFeaturer + "]";
        }
    }
}
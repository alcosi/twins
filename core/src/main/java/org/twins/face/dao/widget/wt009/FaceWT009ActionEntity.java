package org.twins.face.dao.widget.wt009;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt009_action")
public class FaceWT009ActionEntity implements EasyLoggable, Identifiable<UUID> {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt009_id")
    private UUID faceWT009Id;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinActionId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_wt009_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT009Entity faceWT009SpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT009Entity faceWT009;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT009Action[" + id + "]";
            default:
                return "faceWT009Action[id:" + id + ", actionId:" + twinActionId + ", faceWT009id:" + faceWT009Id + "]";
        }
    }
}

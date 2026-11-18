package org.twins.face.dao.widget.wt009;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.action.TwinActionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt009_action")
public class FaceWT009ActionEntity implements EasyLoggable {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_wt009_id", nullable = false, insertable = false, updatable = false)
    private FaceWT009Entity faceWT009;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_action_id", nullable = false, insertable = false, updatable = false)
    private TwinActionEntity twinActionEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;


    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT009Action[" + id + "]";
            default:
                return "faceWT009Action[id:" + id + ", actionId:" + twinActionId + ", faceWT009id:" + faceWT009Id + "]";
        }
    }
}

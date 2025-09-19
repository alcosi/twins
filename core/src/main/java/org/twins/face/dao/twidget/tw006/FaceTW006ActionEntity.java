package org.twins.face.dao.twidget.tw006;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.dao.action.TwinActionEntity;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tw006_action")
public class FaceTW006ActionEntity implements EasyLoggable {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_tw006_id")
    private UUID faceTW006Id;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinActionId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_tw006_id", nullable = false, insertable = false, updatable = false)
    private FaceTW006Entity faceTW006;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_action_id", nullable = false, insertable = false, updatable = false)
    private TwinActionEntity twinActionEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    private I18nEntity labelI18n;


    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW006Action[" + id + "]";
            default:
                return "faceTW006Action[id:" + id + ", actionId:" + twinActionId + ", faceTW006id:" + faceTW006Id + "]";
        }
    }
}

package org.twins.face.dao.twidget.tw005;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

import java.util.UUID;

@Data
@Entity
@Table(name = "face_tw005_button")
public class FaceTW005ButtonEntity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_tw005_id")
    private UUID faceTW005Id;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "twinflow_transition_id")
    private UUID transitionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "show_when_inactive")
    private boolean showWhenInactive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_transition_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity transition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity labelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "faceTW005Button[" + faceTW005Id + "]";
            default:
                return "faceTW005Button[id:" + faceTW005Id + "]";
        }
    }
}

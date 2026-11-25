package org.twins.face.dao.widget.wt007;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt007_button")
public class FaceWT007ButtonEntity implements FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt007_id")
    private UUID faceWT007Id;

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
    private Boolean active;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "show_when_inactive")
    private Boolean showWhenInactive;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_wt007_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceWT007Entity faceWT007SpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRuleSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_transition_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity transitionSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowTransitionEntity transition;

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
    private FaceWT007Entity faceWT007;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "faceWT007Button[" + faceWT007Id + "]";
            default:
                return "faceWT007Button[id:" + faceWT007Id + "]";
        }
    }
}

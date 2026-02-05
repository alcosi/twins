package org.twins.face.dao.twidget.tw005;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Data
@Entity
@Entity
@DomainSetting
@Table(name = "face_tw005")
public class FaceTW005Entity implements EasyLoggable, FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "align_vertical", nullable = false)
    private boolean alignVertical;

    @Column(name = "glue", nullable = false)
    private boolean glue;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceTW005ButtonEntity, UUID> buttons;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW005[" + id + "]";
            default:
                return "faceTW005[id:" + id + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}

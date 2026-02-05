package org.twins.face.dao.page.pg001;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Data
@Entity
@DomainSetting
@Table(name = "face_pg001_widget")
public class FacePG001WidgetEntity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_pg001_id")
    private UUID facePG001Id;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "widget_face_id")
    private UUID widgetFaceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "widget_face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity widgetFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "facePG001Widget[" + id + "]";
            default:
                return "facePG001Widget[id:" + id + ", faceId:" + facePG001Id + "]";
        }
    }
}
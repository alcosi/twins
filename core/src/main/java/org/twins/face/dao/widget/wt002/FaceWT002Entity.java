package org.twins.face.dao.widget.wt002;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceTwinPointerValidatorRuleEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt002")
public class FaceWT002Entity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "face_twin_pointer_validator_rule_id")
    private UUID faceTwinPointerValidatorRuleId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "style_classes")
    private String styleClasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_twin_pointer_validator_rule_id", insertable = false, updatable = false)
    private FaceTwinPointerValidatorRuleEntity faceTwinPointerValidatorRule;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Kit<FaceWT002ButtonEntity, UUID> buttons;
}

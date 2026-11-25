package org.twins.face.dao.widget.wt008;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.comment.TwinCommentSearchEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "face_wt008")
public class FaceWT008Entity implements FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "key")
    private String key;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_comment_search_id")
    private UUID twinCommentSearchId;

    @Column(name = "style_classes")
    private String styleClasses;

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
    @JoinColumn(name = "target_twin_pointer_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity targetTwinPointerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerEntity targetTwinPointer;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_comment_search_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinCommentSearchEntity twinCommentSearchSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinCommentSearchEntity twinCommentSearch;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity faceSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceWT008[" + faceId + "]";
            default:
                return "faceWT008[id:" + faceId + (face != null ? ", componentId:" + face.getFaceComponentId() : "") + "]";
        }
    }
}

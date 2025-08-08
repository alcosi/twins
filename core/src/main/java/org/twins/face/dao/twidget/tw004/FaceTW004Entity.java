package org.twins.face.dao.twidget.tw004;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Table(name = "face_tw004")
public class FaceTW004Entity implements EasyLoggable, FacePointedEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_class_field_search_id", insertable = false, updatable = false)
    private UUID twinClassFieldSearchId;

    @Column(name = "style_classes")
    private String styleClasses;

    @Column(name = "editable_field_filter_featurer_id", insertable = false, updatable = false)
    private Integer fieldFilterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "editable_field_filter_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldFilterParams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", nullable = false, insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity labelI18n;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW004[" + faceId + "]";
            default:
                return "faceTW004[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}

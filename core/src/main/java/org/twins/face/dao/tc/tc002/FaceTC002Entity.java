package org.twins.face.dao.tc.tc002;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_tc002")
public class FaceTC002Entity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id", nullable = false)
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "save_button_label_i18n_id")
    private UUID saveButtonLabelI18nId;

    @Column(name = "header_i18n_id")
    private UUID headerI18nId;

    @Column(name = "header_icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "style_classes")
    private String styleClasses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", nullable = false, insertable = false, updatable = false)
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_i18n_id", insertable = false, updatable = false)
    private I18nEntity headerI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_icon_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconResource;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FaceTC002OptionEntity, UUID> options;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "faceTC002[" + faceId + "]";
            default:
                return "faceTC002[id:" + faceId + ", componentId:" + face.getFaceComponentId() + "]";
        }
    }
}

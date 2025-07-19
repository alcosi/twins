package org.twins.face.dao.tc.tc002;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@Table(name = "face_tc002_option")
public class FaceTC002OptionEntity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_tc002_id", nullable = false)
    private UUID faceTC002Id;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "class_selector_label_i18n_id")
    private UUID classSelectorLabelI18nId;

    @Column(name = "twin_class_id", nullable = false)
    private UUID twinClassId;

    @Column(name = "extends_depth", nullable = false)
    private Integer extendsDepth = 0;

    @Column(name = "head_twin_pointer_id")
    private UUID headTwinPointerId;

    @Column(name = "twin_class_field_search_id")
    private UUID twinClassFieldSearchId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", nullable = false, insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        switch (level) {
            case SHORT:
                return "faceTC002Option[" + id + "]";
            default:
                return "faceTC002Option[id:" + id + ", faceTC002Id:" + faceTC002Id + "]";
        }
    }

}

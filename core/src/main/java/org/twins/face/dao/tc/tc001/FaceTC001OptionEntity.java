package org.twins.face.dao.tc.tc001;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@Table(name = "face_tc001_option")
public class FaceTC001OptionEntity implements EasyLoggable, FaceVariantEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_tc001_id", nullable = false)
    private UUID faceTC001Id;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "class_selector_label_i18n_id")
    private UUID classSelectorLabelI18nId;

    @Column(name = "twin_class_search_id")
    private UUID twinClassSearchId;

    @Column(name = "head_twin_pointer_id")
    private UUID headTwinPointerId;

    @Column(name = "twin_class_field_search_id")
    private UUID twinClassFieldSearchId;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTC001Option[" + id + "]";
            default:
                return "faceTC001Option[id:" + id + ", faceTC001Id:" + faceTC001Id + "]";
        }
    }

}

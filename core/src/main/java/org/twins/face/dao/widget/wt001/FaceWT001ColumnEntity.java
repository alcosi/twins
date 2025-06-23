package org.twins.face.dao.widget.wt001;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceVariant;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_wt001_column")
public class FaceWT001ColumnEntity implements EasyLoggable, FaceVariant{
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_wt001_id")
    private UUID faceWT001Id;

    @Column(name = "face_pointer_validator_rule_id")
    private UUID facePointerValidatorRuleId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "label_i18n_id")
    private UUID labelI18nId;

    @Column(name = "show_by_default")
    private Boolean showByDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_pointer_validator_rule_id", insertable = false, updatable = false)
    private FacePointerValidatorRuleEntity facePointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", nullable = false, insertable = false, updatable = false)
    private TwinClassFieldEntity twinClassField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_i18n_id", insertable = false, updatable = false)
    private I18nEntity labelI18n;

    @Override
    public String easyLog(Level level) {
        return "faceWT001Column[" + id + "]";
    }
}

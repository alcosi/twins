package org.twins.face.dao.twidget.tw007;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinPointerValidatorRuleEntity;
import org.twins.core.dao.twinclass.TwinClassSearchEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "face_tw007")
public class FaceTW007Entity implements EasyLoggable, FacePointedEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "face_id")
    private UUID faceId;

    @Column(name = "twin_pointer_validator_rule_id")
    private UUID twinPointerValidatorRuleId;

    @Column(name = "twin_class_search_id")
    private UUID twinClassSearchId;

    @Column(name = "class_selector_label_i18n_id")
    private UUID classSelectorLabelI18nId;

    @Column(name = "target_twin_pointer_id")
    private UUID targetTwinPointerId;

    @Column(name = "icon_resource_id")
    private UUID iconResourceId;

    @Column(name = "label_id")
    private UUID labelId;

    @Column(name = "save_changes_label_i18n_id")
    private UUID saveChangesLabelI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity face;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_pointer_validator_rule_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinPointerValidatorRuleEntity twinPointerValidatorRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_search_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassSearchEntity twinClassSearch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_selector_label_i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity classSelectorLabelI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_resource_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ResourceEntity iconResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "save_changes_label_i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity saveChangesLabelI18n;

    @Override
    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "faceTW007[" + id + "]";
            default:
                return "faceTW007[id:" + id + ", name:" + face.getName() + "]";
        }
    }
}

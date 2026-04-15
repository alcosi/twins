package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "action_restriction_reason")
public class ActionRestrictionReasonEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "type")
    private String type;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private I18nEntity descriptionI18n;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "actionRestrictionReason[" + id + "]";
            case NORMAL -> "actionRestrictionReason[id:" + id + ", type:" + type + "]";
            default -> "actionRestrictionReason[id:" + id + ", domainId:" + domainId + ", type:" + type + ", descriptionI18nId:" + descriptionI18nId + "]";
        };
    }
}

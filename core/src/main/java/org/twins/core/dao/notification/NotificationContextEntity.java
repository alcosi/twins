package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Entity
@Table(name = "notification_context")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationContextEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    public String easyLog(Level level) {
        return "notificationContext[id:" + id + "]";
    }
}

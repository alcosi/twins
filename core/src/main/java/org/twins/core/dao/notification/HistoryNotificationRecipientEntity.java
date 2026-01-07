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

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "history_notification_recipient")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class HistoryNotificationRecipientEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

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
        return "historyNotificationRecipient[id:" + id + "]";
    }
}

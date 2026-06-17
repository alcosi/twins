package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notification_schema")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationSchemaEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    // Runtime field — populated by mappers, consumed by services. Never read by JPA.
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

    public String easyLog(Level level) {
        return "notificationSchema[id:" + id + "]";
    }
}


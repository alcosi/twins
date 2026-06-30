package org.twins.core.dao.notification;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.twins.core.dao.i18n.I18nTranslationEntity;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notification_context")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class NotificationContextEntity implements EasyLoggable {
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

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table.
    // HACK: @Access(PROPERTY) + NOOP getter/setter — see TwinClassFieldEntity.nameI18nTranslationsSpecOnly for explanation
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Access(AccessType.PROPERTY)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    public List<I18nTranslationEntity> getNameI18nTranslationsSpecOnly() {
        return null;
    }

    public void setNameI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
        // NOOP
    }

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Access(AccessType.PROPERTY)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "description_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> descriptionI18nTranslationsSpecOnly;

    public List<I18nTranslationEntity> getDescriptionI18nTranslationsSpecOnly() {
        return null;
    }

    public void setDescriptionI18nTranslationsSpecOnly(List<I18nTranslationEntity> value) {
        // NOOP
    }

    public String easyLog(Level level) {
        return "notificationContext[id:" + id + "]";
    }
}

package org.twins.core.dao.eraseflow;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "eraseflow")
@FieldNameConstants
public class EraseflowEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "target_deletion_factory_id")
    private UUID targetDeletionFactoryId;

    @Column(name = "cascade_deletion_by_head_factory_id")
    private UUID cascadeDeletionByHeadFactoryId;

    @Column(name = "cascade_deletion_by_link_default_factory_id")
    private UUID cascadeDeletionByLinkDefaultFactoryId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClassSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUserSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table.
    // HACK: @Access(PROPERTY) + NOOP getter/setter — see TwinClassFieldEntity.nameI18nTranslationsSpecOnly for explanation
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<EraseflowLinkCascadeEntity, UUID> cascadeLinkKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @Override
    public String easyLog(Level level) {
        return "eraseflow[id:" + id + "]";
    }
}

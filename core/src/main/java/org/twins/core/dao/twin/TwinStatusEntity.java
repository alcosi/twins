package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.status.StatusType;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_status")
@FieldNameConstants
public class TwinStatusEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "inheritable")
    private Boolean inheritable;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "icon_light_resource_id")
    private UUID iconLightResourceId;

    @Column(name = "icon_dark_resource_id")
    private UUID iconDarkResourceId;

    @Column(name = "background_color")
    private String backgroundColor;

    @Column(name = "font_color")
    private String fontColor;

    @Enumerated(EnumType.STRING)
    @Column(name = "twin_status_type")
    private StatusType type;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_light_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconLightResource;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_dark_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconDarkResource;

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

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinStatus[" + id + "]";
            case NORMAL -> "twinStatus[id:" + id + ", key:" + key + "]";
            default -> "twinStatus[id:" + id + ", twinClassId:" + twinClassId + "]";
        };
    }
}

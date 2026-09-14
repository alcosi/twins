package org.twins.core.dao.datalist;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "data_list_subset")
@FieldNameConstants
public class DataListSubsetEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "data_list_id")
    private UUID dataListId;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "key")
    private String key;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "data_list_id", insertable = false, updatable = false)
    private DataListEntity dataListSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    DataListEntity dataList;

    @Deprecated // for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUserSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    UserEntity createdByUser;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table.
    // HACK: @Access(PROPERTY) + NOOP getter/setter — see TwinClassFieldEntity.nameI18nTranslationsSpecOnly for explanation
    @Deprecated // for specification only
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
    @Deprecated // for specification only
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

    public String easyLog(Level level) {
        return "dataListSubset[id:" + id + ", key:" + key + "]";
    }
}

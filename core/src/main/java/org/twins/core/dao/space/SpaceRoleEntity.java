package org.twins.core.dao.space;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Accessors(chain = true)
@Table(name = "space_role")
public class SpaceRoleEntity implements EasyLoggable  {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "description_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> descriptionI18nTranslationsSpecOnly;

    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "spaceRole[" + id + "]";
            case NORMAL -> "spaceRole[id:" + id + ", key:" + key + "]";
            default -> "spaceRole[id:" + id + ", key:" + key + ", twinClassId:" + twinClassId + ", businessAccountId:" + businessAccountId + "]";
        };
    }
}

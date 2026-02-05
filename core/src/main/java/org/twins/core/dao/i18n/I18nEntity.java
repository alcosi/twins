package org.twins.core.dao.i18n;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;
import org.twins.core.enums.i18n.I18nType;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "i18n")
@DomainSetting
@FieldNameConstants
public class I18nEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Basic
    @Column(name = "name")
    private String name;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "i18n_type_id")
    @Convert(converter = I18nTypeConverter.class)
    private I18nType type;

    @Basic
    @Column(name = "key")
    private String key;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "i18n_type_id", insertable = false, updatable = false)
    private I18nTypeEntity i18nType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @OneToMany(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    @JsonIgnore
    private List<I18nTranslationEntity> translations;

    @Transient
    private Kit<I18nTranslationEntity, Locale> translationsKit;

    @Transient
    public I18nEntity addTranslation(I18nTranslationEntity translationEntity) {
        if (translationsKit == null)
            translationsKit = new Kit<>(I18nTranslationEntity::getLocale);
        translationsKit.add(translationEntity);
        return this;
    }

    @Transient
    private Kit<I18nTranslationBinEntity, Locale> translationsBin;

    @Override
    public String toString() {
        return "I18NEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

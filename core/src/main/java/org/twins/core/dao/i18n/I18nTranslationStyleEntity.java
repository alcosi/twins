package org.twins.core.dao.i18n;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Table(name = "i18n_translation_style")
@DomainSetting
@FieldNameConstants
public class I18nTranslationStyleEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "i18n_id")
    private UUID i18nId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    private I18nEntity i18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "color")
    private String color;

    @Column(name = "size")
    private String size;

    @Column(name = "mask")
    private String mask;

    @Column(name = "link")
    private String link;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;
}

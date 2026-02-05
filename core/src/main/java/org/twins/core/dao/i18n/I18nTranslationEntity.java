package org.twins.core.dao.i18n;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.io.Serializable;
import java.util.*;

@Slf4j
@Entity
@Data
@Accessors(chain = true)
@Table(name = "i18n_translation")
@DomainSetting
@IdClass(I18nTranslationEntity.PK.class)
@FieldNameConstants
public class I18nTranslationEntity implements EasyLoggable {
    @Id
    @Column(name = "i18n_id")
    private UUID i18nId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private I18nEntity i18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Id
    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Basic
    @Column(name = "translation")
    private String translation;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    /**
     * this counter is used to indicate how many times this translation was
     * requested, but it was missed.
     * this can help to detect most awaited translations. must be incremented only
     * if translation is empty
     */
    @Basic
    @Column(name = "usage_counter")
    private Integer usageCounter = 0;

    @Transient
    private MultipartFile file;

    @Transient
    private List<I18nTranslationStyleEntity> styles;

    @Transient
    public String getKitKey() {
        return i18nId + "." + locale.getLanguage();
    }

    @Transient
    public void addStyle(I18nTranslationStyleEntity style) {
        if (styles == null)
            styles = new ArrayList<>();
        styles.add(style);
    }

    @Transient
    public String fillContent(Map<String, String> context) {
        if (MapUtils.isNotEmpty(context))
            return StringUtils.replaceVariables(translation, context);
        else
            return translation;
    }

    public static String getCSVHeaders() {
        return "id;default;locale;content;";
    }

    public static I18nTranslationEntity fromCSV(String s) {
        try {
            if (s == null || s.isEmpty()) {
                return null;
            } else {
                String[] parts = s.split(";");
                if (parts.length < 4) {
                    log.error("Line [{}] has incorrect column count[{}]", s, parts.length);
                    return null;
                }
                I18nTranslationEntity translationEntity = new I18nTranslationEntity();
                try {
                    translationEntity.setI18nId(UUID.fromString(parts[0]));
                } catch (Exception e) {
                    log.error("Incorrect UUID format[{}]", parts[0]);
                    return null;
                }
                translationEntity.setLocale(Locale.forLanguageTag(parts[2]));
                if (StringUtils.isNotBlank(parts[3])) {
                    parts[3] = parts[3].replaceAll("<br>", "\r\n");
                }
                translationEntity.setTranslation(parts[3]);
                return translationEntity;
            }
        } catch (Throwable t) {
            log.error("Exception");
            return null;
        }
    }

    @Override
    public String easyLog(Level level) {
        return "i18nTranslation[i18nId:" + i18nId + ", locale:" + locale + "]";
    }

    @Data
    protected static class PK implements Serializable {
        @Column(name = "i18n_id")
        private UUID i18nId;

        @Column(name = "locale")
        @Convert(converter = LocaleConverter.class)
        private Locale locale;
    }
}

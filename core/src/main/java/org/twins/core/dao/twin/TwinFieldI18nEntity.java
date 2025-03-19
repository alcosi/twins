package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_i18n")
public class TwinFieldI18nEntity implements TwinFieldStorage {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "i18n_id")
    private UUID i18nId;

    @Transient
    private String translationsString;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @ManyToOne
    @JoinColumn(name = "i18n_id", insertable = false, updatable = false, nullable = false)
    private I18nEntity i18n;

    public TwinFieldI18nEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldI18nEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassFieldId(twinClassFieldId)
                .setTwinClassField(twinClassField)
                .setI18n(i18n)
                .setI18nId(i18nId);
    }

    public String getTranslationsString() {
        if (this.i18n == null || this.i18n.getTranslations() == null) {
            return "";
        }

        return this.i18n.getTranslations().stream()
                .map(translation -> "<@entryKey>" + translation.getLocale() + "<@entryValue>" + translation.getTranslation() + "<@mapEntry>")
                .collect(Collectors.joining());
    }

    public TwinFieldI18nEntity setTranslationsString(String translationsString) {
        if (translationsString == null || translationsString.isEmpty()) {
            return this;
        }

        Map<Locale, String> translationsMap = parseTranslations(translationsString);
        List<I18nTranslationEntity> translations = translationsMap.entrySet().stream()
                .map(entry -> new I18nTranslationEntity()
                        .setLocale(entry.getKey())
                        .setTranslation(entry.getValue()))
                .collect(Collectors.toList());

        if (this.i18n == null) {
            this.i18n = new I18nEntity();
        }
        this.i18n.setTranslations(translations);

        return this;
    }

    private Map<Locale, String> parseTranslations(String translationsString) {
        Map<Locale, String> translations = new HashMap<>();
        if (translationsString == null || translationsString.isEmpty()) {
            return translations;
        }

        String[] entries = translationsString.split("<@mapEntry>");
        for (String entry : entries) {
            if (entry.contains("<@entryKey>") && entry.contains("<@entryValue>")) {
                String[] parts = entry.split("<@entryValue>");
                String localeStr = parts[0].replace("<@entryKey>", "").trim();
                String translation = parts[1].trim();
                translations.put(Locale.forLanguageTag(localeStr), translation);
            }
        }
        return translations;
    }
}




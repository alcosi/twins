package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.MapUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueI18n extends FieldValue {
    public Map<Locale, String> translations;

    public FieldValueI18n(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
        this.translations = new HashMap<>();
    }

    public Map<Locale, String> getTranslations() {
        return translations == null
                ? Collections.EMPTY_MAP
                : Collections.unmodifiableMap(translations);
    }

    public FieldValueI18n setTranslations(Map<Locale, String> newTranslations) {
        if (CollectionUtils.isEmpty(newTranslations)) {
            translations = null;
            state = State.CLEARED;
        } else {
            translations = new HashMap<>(newTranslations); //new map here
            state = State.PRESENT;
        }
        return this;
    }

    public void addTranslation(Locale locale, String translation) {
        if (translations == null) {
            translations = new HashMap<>();
        }
        translations.put(locale, translation);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueI18n clone = new FieldValueI18n(newTwinClassFieldEntity);
        clone.translations = new HashMap<>(this.translations);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return !MapUtils.isEmpty(translations) && translations.containsValue(value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        translations = new HashMap<>(((FieldValueI18n) src).translations);
    }

    @Override
    public void onUndefine() {
        translations = null;
    }

    @Override
    public void onClear() {
        translations = null;
    }
}

package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.MapUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueI18n extends FieldValue {
    public Map<Locale, String> translations;

    public FieldValueI18n(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
        this.translations = new HashMap<>(); // Инициализация пустой карты
    }

    public void addTranslation(Locale locale, String translation) {
        if (translations == null) {
            translations = new HashMap<>();
        }
        translations.put(locale, translation);
    }

    @Override
    public boolean isFilled() {
        return MapUtils.isNotEmpty(translations);
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueI18n clone = new FieldValueI18n(newTwinClassFieldEntity);
        clone.setTranslations(new HashMap<>(this.translations));
        return clone;
    }

    @Override
    public void nullify() {
        translations = null;
    }

    @Override
    public boolean hasValue(String value) {
        return !MapUtils.isEmpty(translations) && translations.containsValue(value);
    }
}

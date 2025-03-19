package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueI18n extends FieldValue {
    private UUID i18nId;
    private List<I18nTranslationEntity> i18nTranslations;

    public FieldValueI18n(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return i18nId != null && i18nTranslations != null && !i18nTranslations.isEmpty();
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueI18n clone = new FieldValueI18n(newTwinClassFieldEntity);
        clone.setI18nId(this.i18nId);
        clone.setI18nTranslations(List.copyOf(this.i18nTranslations));
        return clone;
    }

    @Override
    public void nullify() {
        i18nId = null;
        i18nTranslations = null;
    }

    @Override
    public boolean hasValue(String value) {
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return i18nId != null && i18nId.equals(valueUUID);
    }
}

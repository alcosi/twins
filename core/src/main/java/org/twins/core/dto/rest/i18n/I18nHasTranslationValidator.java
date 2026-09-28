package org.twins.core.dto.rest.i18n;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class I18nHasTranslationValidator implements ConstraintValidator<I18nHasTranslation, I18nSaveDTOv1> {

    @Override
    public boolean isValid(I18nSaveDTOv1 value, ConstraintValidatorContext context) {
        if (value == null)
            return true; // null is the responsibility of @NotNull
        if (StringUtils.isNotBlank(value.getTranslationInCurrentLocale()))
            return true;
        if (value.getTranslations() != null)
            return value.getTranslations().values().stream().anyMatch(StringUtils::isNotBlank);
        return false;
    }
}

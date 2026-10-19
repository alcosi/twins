package org.twins.core.dto.rest.i18n;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class I18nHasTranslationValidatorTest {

    private final I18nHasTranslationValidator validator = new I18nHasTranslationValidator();

    @Test
    public void testNullField_IsValid_ForNotNullToHandle() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    public void testEmptyObject_IsInvalid() {
        assertFalse(validator.isValid(new I18nSaveDTOv1(), null));
    }

    @Test
    public void testBlankContentOnly_IsInvalid() {
        I18nSaveDTOv1 dto = new I18nSaveDTOv1()
                .setTranslationInCurrentLocale("   ");
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.ENGLISH, "");
        dto.setTranslations(translations);
        assertFalse(validator.isValid(dto, null));
    }

    @Test
    public void testTranslationInCurrentLocale_IsValid() {
        I18nSaveDTOv1 dto = new I18nSaveDTOv1()
                .setTranslationInCurrentLocale("Oak");
        assertTrue(validator.isValid(dto, null));
    }

    @Test
    public void testTranslationsMap_IsValid() {
        Map<Locale, String> translations = new HashMap<>();
        translations.put(Locale.ENGLISH, "Oak");
        translations.put(Locale.GERMAN, "Eiche");
        I18nSaveDTOv1 dto = new I18nSaveDTOv1()
                .setTranslations(translations);
        assertTrue(validator.isValid(dto, null));
    }
}

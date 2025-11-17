package org.twins.core.dao.i18n;

import org.twins.core.enums.i18n.I18nType;

import java.util.Locale;
import java.util.UUID;

public record I18nTranslationLight(UUID i18nId, I18nType i18nType, Locale locale, String translation) {}
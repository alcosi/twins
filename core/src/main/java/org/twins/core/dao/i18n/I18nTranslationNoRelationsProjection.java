package org.twins.core.dao.i18n;

import org.cambium.common.EasyLoggable;

import java.util.UUID;

public record I18nTranslationNoRelationsProjection(
        UUID i18nId,
        String translation
) implements EasyLoggable {
    public String easyLog(Level level) {
        return "i18nTranslation[i18nId:" + i18nId + ", translation:" + translation + "]";
    }
}

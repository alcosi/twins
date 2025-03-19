package org.twins.core.domain.i18n;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class I18nSave {
    public UUID i18nId;
    public Map<Locale, String> translations;
}

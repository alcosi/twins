package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "I18nTranslationV1")
public class I18nTranslationDTOv1 {
    @Schema(description = "map(uuid : map(locale : translation))")
    public Map<UUID, Map<Locale, String>> i18nTranslations;
}

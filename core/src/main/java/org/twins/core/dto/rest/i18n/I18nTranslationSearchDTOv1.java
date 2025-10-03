package org.twins.core.dto.rest.i18n;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.math.LongRange;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "I18nTranslationSearchV1")
public class I18nTranslationSearchDTOv1 {
    @Schema(description = "i18n id list")
    public Set<UUID> i18nIdList;

    @Schema(description = "i18n id exclude list")
    public Set<UUID> i18nIdExcludeList;

    @Schema(description = "translation like list")
    public Set<String> translationLikeList;

    @Schema(description = "translation not like list")
    public Set<String> translationNotLikeList;

    @Schema(description = "locale like list")
    public Set<Locale> localeLikeList;

    @Schema(description = "locale not like list")
    public Set<Locale> localeNotLikeList;

    @Schema(description = "usage counter")
    public LongRange usageCounter;
}

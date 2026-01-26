package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.LongRange;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class I18nTranslationSearch {
    private Set<UUID> i18nIdList;
    private Set<UUID> i18nIdExcludeList;
    private Set<String> translationLikeList;
    private Set<String> translationNotLikeList;
    private Set<Locale> localeLikeList;
    private Set<Locale> localeNotLikeList;
    private LongRange usageCounter;
}

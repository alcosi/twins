package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.i18n.I18nEntity;

@FeaturerParamType(
        id = "UUID:TWINS:I18N_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = I18nEntity.class)
public class FeaturerParamUUIDTwinsI18nId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsI18nId(String key) {
        super(key);
    }
}

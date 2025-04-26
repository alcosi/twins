package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;

import java.util.Properties;
import java.util.UUID;

@FeaturerParamType(
        id = "UUID:TWINS:RESTRICTION_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE)
public class FeaturerParamUUIDTwinsAttachmentRestrictionId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsAttachmentRestrictionId(String key) {
        super(key);
    }

    @Override
    public UUID extract(Properties properties) {
        if (!properties.containsKey(key))
            return null;
        return super.extract(properties);
    }
}

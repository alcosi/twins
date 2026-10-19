package org.twins.core.featurer.params;

import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;

@FeaturerParamType(
        id = "UUID:TWINS:RESTRICTION_ID",
        description = "",
        regexp = FeaturerParamUUID.UUID_REGEXP,
        example = FeaturerParamUUID.UUID_EXAMPLE,
        targetEntity = TwinAttachmentRestrictionEntity.class)
public class FeaturerParamUUIDTwinsAttachmentRestrictionId extends FeaturerParamUUID {
    public FeaturerParamUUIDTwinsAttachmentRestrictionId(String key) {
        super(key);
    }
}

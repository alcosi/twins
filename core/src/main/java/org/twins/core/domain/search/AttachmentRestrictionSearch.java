package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.IntegerRange;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class AttachmentRestrictionSearch extends EntitySearch<TwinAttachmentRestrictionEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private IntegerRange minCountRange;
    private IntegerRange maxCountRange;
    private IntegerRange fileSizeMbLimitRange;
    private Set<String> fileExtensionLimitLikeList;
    private Set<String> fileExtensionLimitNotLikeList;
    private Set<String> fileNameRegexpLikeList;
    private Set<String> fileNameRegexpNotLikeList;
}

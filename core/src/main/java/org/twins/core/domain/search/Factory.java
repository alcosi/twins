package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class Factory {
    public Set<UUID> idList;
    public Set<UUID> idExcludeList;
    public Set<String> keyLikeList;
    public Set<String> keyNotLikeList;
    public Set<String> nameI18nLikeList;
    public Set<String> nameI18nNotLikeList;
    public Set<String> descriptionI18nLikeList;
    public Set<String> descriptionI18nNotLikeList;
}

package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TransitionAliasSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> aliasLikeList;
    private Set<String> aliasNotLikeList;
}

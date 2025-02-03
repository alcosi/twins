package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserGroupSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> nameI18NLikeList;
    private Set<String> nameI18nNotLikeList;
    private Set<String> descriptionI18NLikeList;
    private Set<String> descriptionI18NNotLikeList;
    private Set<String> typeList;
    private Set<String> typeExcludeList;
}

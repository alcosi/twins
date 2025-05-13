package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.user.UserStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class UserSearch {
    private Set<UUID> userIdList;
    private Set<UUID> userIdExcludeList;
    private Set<String> userNameLikeList;
    private Set<String> userNameLikeExcludeList;
    private Set<String> userEmailLikeList;
    private Set<String> userEmailLikeExcludeList;
    private Set<UserStatus> statusIdList;
    private Set<UserStatus> statusIdExcludeList;
    private List<SpaceSearch> spaceList;
    private List<SpaceSearch> spaceExcludeList;
    private BasicSearchList childTwinSearches;
}

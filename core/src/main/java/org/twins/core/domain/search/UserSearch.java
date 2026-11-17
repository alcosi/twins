package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.user.UserSearchEntity;
import org.twins.core.enums.user.UserStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class UserSearch implements ConfiguredSearchHolder<UserSearchEntity> {
    @SearchFilter
    private Set<UUID> userIdList;

    @SearchFilter
    private Set<UUID> userIdExcludeList;

    @SearchFilter
    private Set<String> userNameLikeList;

    @SearchFilter
    private Set<String> userNameLikeExcludeList;

    @SearchFilter
    private Set<String> userEmailLikeList;

    @SearchFilter
    private Set<String> userEmailLikeExcludeList;

    @SearchFilter
    private Set<String> userNameOrEmailLikeList;

    @SearchFilter
    private Set<String> userNameOrEmailExcludeList;

    @SearchFilter
    private Set<UUID> userGroupIdList;

    @SearchFilter
    private Set<UUID> userGroupIdExcludeList;

    @SearchFilter
    private Set<UserStatus> statusIdList;

    @SearchFilter
    private Set<UserStatus> statusIdExcludeList;

    private List<SpaceSearch> spaceList;
    private List<SpaceSearch> spaceExcludeList;
    private BasicSearchList childTwinSearches;
    private UserSearchEntity configuredSearch;
    private boolean emptySearch = false;

    public UserSearch addSpace(final SpaceSearch spaceSearch, boolean exclude) {
        if (exclude)
            spaceExcludeList = CollectionUtils.safeAdd(spaceExcludeList, spaceSearch);
        else
            spaceList = CollectionUtils.safeAdd(spaceList, spaceSearch);
        return this;
    }
}

package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.user.UserSearchEntity;
import org.twins.core.enums.user.UserStatus;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private Set<String> userNameOrEmailLikeList;
    private Set<String> userNameOrEmailExcludeList;
    private Set<UUID> userGroupIdList;
    private Set<UUID> userGroupIdExcludeList;
    private Set<UserStatus> statusIdList;
    private Set<UserStatus> statusIdExcludeList;
    private List<SpaceSearch> spaceList;
    private List<SpaceSearch> spaceExcludeList;
    private BasicSearchList childTwinSearches;
    private UserSearchEntity configuredSearch;

    public UserSearch addSpace(final SpaceSearch spaceSearch, boolean exclude) {
        if (exclude)
            spaceExcludeList = CollectionUtils.safeAdd(spaceExcludeList, spaceSearch);
        else
            spaceList = CollectionUtils.safeAdd(spaceList, spaceSearch);
        return this;
    }

    public static final ImmutableList<Pair<Function<UserSearch, Set>, BiConsumer<UserSearch, Set>>> FUNCTIONS = ImmutableList.of(
            Pair.of(UserSearch::getUserIdList, UserSearch::setUserIdList),
            Pair.of(UserSearch::getUserIdExcludeList, UserSearch::setUserIdExcludeList),
            Pair.of(UserSearch::getUserNameLikeList, UserSearch::setUserNameLikeList),
            Pair.of(UserSearch::getUserNameLikeExcludeList, UserSearch::setUserNameLikeExcludeList),
            Pair.of(UserSearch::getUserEmailLikeList, UserSearch::setUserEmailLikeList),
            Pair.of(UserSearch::getUserEmailLikeExcludeList, UserSearch::setUserEmailLikeExcludeList),
            Pair.of(UserSearch::getUserNameOrEmailLikeList, UserSearch::setUserNameOrEmailLikeList),
            Pair.of(UserSearch::getUserNameOrEmailExcludeList, UserSearch::setUserNameOrEmailExcludeList),
            Pair.of(UserSearch::getUserNameOrEmailExcludeList, UserSearch::setUserNameOrEmailExcludeList),
            Pair.of(UserSearch::getUserGroupIdList, UserSearch::setUserGroupIdList),
            Pair.of(UserSearch::getUserGroupIdExcludeList, UserSearch::setUserGroupIdExcludeList),
            Pair.of(UserSearch::getStatusIdList, UserSearch::setStatusIdList),
            Pair.of(UserSearch::getStatusIdExcludeList, UserSearch::setStatusIdExcludeList)
    );
}

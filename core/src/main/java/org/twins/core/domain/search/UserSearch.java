package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.user.UserStatus;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;

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
    private Set<UserStatus> statusIdList;
    private Set<UserStatus> statusIdExcludeList;
    private List<SpaceSearch> spaceList;
    private List<SpaceSearch> spaceExcludeList;
    private TwinConditionSearch childTwinsCondition;
}

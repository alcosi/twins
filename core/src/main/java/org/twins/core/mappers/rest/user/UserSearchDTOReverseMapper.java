package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.dto.rest.user.UserSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class UserSearchDTOReverseMapper extends RestSimpleDTOMapper<UserSearchDTOv1, UserSearch> {
    @Override
    public void map(UserSearchDTOv1 src, UserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserIdList(src.getUserIdList())
                .setUserIdExcludeList(src.getUserIdExcludeList())
                .setUserNameLikeList(src.getUserNameLikeList())
                .setUserNameLikeExcludeList(src.getUserNameLikeExcludeList())
                .setStatusIdList(src.getStatusIdList())
                .setStatusIdExcludeList(src.getStatusIdExcludeList())
                .setSpaceList(src.getSpaceList())
                .setSpaceExcludeList(src.getSpaceExcludeList())
                .setChildTwins(src.getChildTwins());
    }
}

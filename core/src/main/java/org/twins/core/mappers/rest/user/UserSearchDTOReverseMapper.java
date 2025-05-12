package org.twins.core.mappers.rest.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SpaceSearch;
import org.twins.core.domain.search.TwinConditionSearch;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.dto.rest.user.SpaceSearchDTOv1;
import org.twins.core.dto.rest.user.UserSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinSearchDTOReverseMapper;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserSearchDTOReverseMapper extends RestSimpleDTOMapper<UserSearchDTOv1, UserSearch> {
    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(UserSearchDTOv1 src, UserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserIdList(src.getUserIdList())
                .setUserIdExcludeList(src.getUserIdExcludeList())
                .setUserNameLikeList(src.getUserNameLikeList())
                .setUserNameLikeExcludeList(src.getUserNameLikeExcludeList())
                .setStatusIdList(src.getStatusIdList())
                .setStatusIdExcludeList(src.getStatusIdExcludeList())
                .setSpaceList(mapSpaceList(src.getSpaceList()))
                .setSpaceExcludeList(mapSpaceList(src.getSpaceExcludeList()))
                .setChildTwinsCondition(new TwinConditionSearch()
                        .setMatchAll(src.getChildTwinsCondition().getMachAll())
                        .setChildTwins(twinSearchDTOReverseMapper.convertCollection(src.getChildTwinsCondition().getChildTwins())));
    }

    private List<SpaceSearch> mapSpaceList(List<SpaceSearchDTOv1> spaceDTOs) {
        if (spaceDTOs == null) {
            return null;
        }
        return spaceDTOs.stream()
                .map(dto -> new SpaceSearch()
                        .setSpaceId(dto.getSpaceId())
                        .setRoleId(dto.getRoleId()))
                .collect(Collectors.toList());
    }
}
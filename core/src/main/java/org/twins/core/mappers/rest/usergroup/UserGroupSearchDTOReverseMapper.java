package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.enums.user.UserGroupType;
import org.twins.core.domain.search.UserGroupSearch;
import org.twins.core.dto.rest.usergroup.UserGroupSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserGroupSearchDTOReverseMapper extends RestSimpleDTOMapper<UserGroupSearchRqDTOv1, UserGroupSearch> {

    @Override
    public void map(UserGroupSearchRqDTOv1 src, UserGroupSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameI18NLikeList(src.getNameI18NLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18NLikeList(src.getDescriptionI18NLikeList())
                .setDescriptionI18NNotLikeList(src.getDescriptionI18NNotLikeList())
                .setTypeList(safeConvert(src.getTypeList()))
                .setTypeExcludeList(safeConvert(src.getTypeExcludeList()));
    }

    private Set<String> safeConvert(Set<UserGroupType> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }
}

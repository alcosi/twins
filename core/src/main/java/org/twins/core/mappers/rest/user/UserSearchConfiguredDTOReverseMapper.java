package org.twins.core.mappers.rest.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.UserSearch;
import org.twins.core.dto.rest.user.UserSearchConfiguredDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserSearchConfiguredDTOReverseMapper extends RestSimpleDTOMapper<UserSearchConfiguredDTOv1, UserSearch> {

    private final UserSearchDTOReverseMapper userSearchDTOReverseMapper;

    @Override
    public void map(UserSearchConfiguredDTOv1 src, UserSearch dst, MapperContext mapperContext) throws Exception {
        if (src.getNarrow() != null) {
            userSearchDTOReverseMapper.map(src.getNarrow(), dst, mapperContext);
        }
    }
}

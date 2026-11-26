package org.twins.core.mappers.rest.user;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.user.UserSortDTOv1;
import org.twins.core.enums.sort.UserSortField;
import org.twins.core.mappers.rest.SortDTOReverseMapper;

@Component
public class UserSortDTOReverseMapper extends SortDTOReverseMapper<UserSortDTOv1, UserSortField> {
    @Override
    protected UserSortField sortField(UserSortDTOv1 dto) {
        return dto.getSortField();
    }
}

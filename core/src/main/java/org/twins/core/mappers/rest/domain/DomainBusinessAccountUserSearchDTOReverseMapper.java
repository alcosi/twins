package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.sort.DomainBusinessAccountUserSortField;
import org.twins.core.domain.search.DomainBusinessAccountUserSearch;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.SortDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountUserSearchDTOReverseMapper extends RestSimpleDTOMapper<DomainBusinessAccountUserSearchDTOv1, DomainBusinessAccountUserSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;
    private final SortDTOReverseMapper sortDTOReverseMapper;

    @Override
    public void map(DomainBusinessAccountUserSearchDTOv1 src, DomainBusinessAccountUserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserIdList(convertToSetSafe(src.getUserIdList()))
                .setUserIdExcludeList(convertToSetSafe(src.getUserIdExcludeList()))
                .setBusinessAccountIdList(convertToSetSafe(src.getBusinessAccountIdList()))
                .setBusinessAccountIdExcludeList(convertToSetSafe(src.getBusinessAccountIdExcludeList()))
                .setUserGroupIdList(convertToSetSafe(src.getUserGroupIdList()))
                .setUserGroupIdExcludeList(convertToSetSafe(src.getUserGroupIdExcludeList()))
                .setLastActivityAtRange(dataTimeRangeDTOReverseMapper.convert(src.getLastActivityAt()))
                .setCreatedAtRange(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
        dst.getSortOption().setIfNotNull(sortDTOReverseMapper.convert(src.getSort(), DomainBusinessAccountUserSortField.class));
    }
}

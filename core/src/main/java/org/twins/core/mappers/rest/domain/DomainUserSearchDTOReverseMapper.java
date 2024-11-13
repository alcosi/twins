package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainUserSearch;
import org.twins.core.dto.rest.domain.DomainUserSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class DomainUserSearchDTOReverseMapper extends RestSimpleDTOMapper<DomainUserSearchRqDTOv1, DomainUserSearch> {

    @Override
    public void map(DomainUserSearchRqDTOv1 src, DomainUserSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserIdList(convertToSetSafe(src.getUserIdList()))
                .setUserIdExcludeList(convertToSetSafe(src.getUserIdExcludeList()))
                .setNameLikeList(convertToSetSafe(src.getNameLikeList()))
                .setNameNotLikeList(convertToSetSafe(src.getNameNotLikeList()))
                .setEmailLikeList(convertToSetSafe(src.getEmailLikeList()))
                .setEmailNotLikeList(convertToSetSafe(src.getEmailNotLikeList()))
                .setStatusIdList(convertToSetSafe(src.getStatusIdList()))
                .setStatusIdExcludeList(convertToSetSafe(src.getStatusIdExcludeList()))
                .setBusinessAccountIdList(convertToSetSafe(src.getBusinessAccountIdList()))
                .setBusinessAccountIdExcludeList(convertToSetSafe(src.getBusinessAccountIdExcludeList()));

    }
}

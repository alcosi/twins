package org.twins.core.mappers.rest.space;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SpaceRoleSearch;
import org.twins.core.dto.rest.space.SpaceRoleSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class SpaceRoleSearchDTOReverseMapper extends RestSimpleDTOMapper<SpaceRoleSearchRqDTOv1, SpaceRoleSearch> {
    @Override
    public void map(SpaceRoleSearchRqDTOv1 src, SpaceRoleSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdList(src.getTwinClassIdList())
                .setTwinClassIdExcludeList(src.getTwinClassIdExcludeList())
                .setBusinessAccountIdList(src.getBusinessAccountIdList())
                .setBusinessAccountIdExcludeList(src.getBusinessAccountIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList());
    }
}

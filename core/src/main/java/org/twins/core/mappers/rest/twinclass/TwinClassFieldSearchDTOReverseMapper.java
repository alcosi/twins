package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinClassFieldSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldSearchRqDTOv1, TwinClassFieldSearch> {
    @Override
    public void map(TwinClassFieldSearchRqDTOv1 src, TwinClassFieldSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList())
                .setFieldTyperIdList(src.getFieldTyperIdList())
                .setFieldTyperIdExcludeList(src.getFieldTyperIdExcludeList())
                .setViewPermissionIdList(src.getViewPermissionIdList())
                .setViewPermissionIdExcludeList(src.getViewPermissionIdExcludeList())
                .setEditPermissionIdList(src.getEditPermissionIdList())
                .setEditPermissionIdExcludeList(src.getEditPermissionIdExcludeList())
                .setRequired(src.getRequired());
    }
}
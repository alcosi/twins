package org.twins.core.mappers.rest.datalist;

import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.datalist.DataListOptionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DataListOptionSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionSearchDTOv1, DataListOptionSearch> {

    @Override
    public void map(DataListOptionSearchDTOv1 src, DataListOptionSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setDataListIdList(src.getDataListIdList())
                .setDataListIdExcludeList(src.getDataListIdExcludeList())
                .setDataListKeyList(src.getDataListKeyList())
                .setDataListKeyExcludeList(src.getDataListKeyExcludeList())
                .setOptionLikeList(src.getOptionLikeList())
                .setOptionNotLikeList(src.getOptionNotLikeList())
                .setOptionI18nLikeList(src.getOptionI18nLikeList())
                .setOptionI18nNotLikeList(src.getOptionI18nNotLikeList())
                .setBusinessAccountIdList(src.getBusinessAccountIdList())
                .setBusinessAccountIdExcludeList(src.getBusinessAccountIdExcludeList())
                .setDataListSubsetIdList(src.getDataListSubsetIdList())
                .setDataListSubsetIdExcludeList(src.getDataListSubsetIdExcludeList())
                .setDataListSubsetKeyList(src.getDataListSubsetKeyList())
                .setDataListSubsetKeyExcludeList(src.getDataListSubsetKeyExcludeList())
                .setStatusIdList(src.getStatusIdList())
                .setStatusIdExcludeList(src.getStatusIdExcludeList())
                .setExternalIdList(CollectionUtils.getFirstNotEmpty(src.getExternalIdList(), src.getExternalIdLikeList()))
                .setExternalIdExcludeList(CollectionUtils.getFirstNotEmpty(src.getExternalIdExcludeList(), src.getExternalIdNotLikeList()))
                .setValidForTwinClassFieldIdList(src.getValidForTwinClassFieldIdList())
                .setCustom(src.getCustom());
    }
}

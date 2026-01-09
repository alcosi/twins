package org.twins.core.mappers.rest.datalist;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.datalist.DataListOptionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Set;

@Component
public class DataListOptionSearchDTOReverseMapper extends RestSimpleDTOMapper<DataListOptionSearchDTOv1, DataListOptionSearch> {

    @Override
    public void map(DataListOptionSearchDTOv1 src, DataListOptionSearch dst, MapperContext mapperContext) {
        // Сначала обрабатываем стандартные поля
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
                .setValidForTwinClassFieldIdList(src.getValidForTwinClassFieldIdList())
                .setCustom(src.getCustom());

        handleExternalIdMapping(src, dst);
    }

    // using deprecated like fields only if main one doesn't exist
    private void handleExternalIdMapping(DataListOptionSearchDTOv1 src, DataListOptionSearch dst) {
        Set<String> externalIdList = src.getExternalIdList();
        Set<String> externalIdExcludeList = src.getExternalIdExcludeList();

        boolean hasStandardExternalId = externalIdList != null && !externalIdList.isEmpty() || externalIdExcludeList != null && !externalIdExcludeList.isEmpty();

        if (hasStandardExternalId) {
            dst.setExternalIdList(externalIdList);
            dst.setExternalIdExcludeList(externalIdExcludeList);
        } else {
            dst.setExternalIdList(src.getExternalIdLikeList());
            dst.setExternalIdExcludeList(src.getExternalIdNotLikeList());
        }
    }
}

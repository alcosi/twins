package org.twins.core.mappers.rest.datalist;

import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.dto.rest.datalist.DataListOptionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
                .setStatusIdList(convertStatusToString(src.getStatusIdList()))
                .setStatusIdExcludeList(convertStatusToString(src.getStatusIdExcludeList()))
        ;
    }

    private Set<String> convertStatusToString(Set<DataListOptionEntity.Status> statusSet) {
        return CollectionUtils.isEmpty(statusSet)
                ? new HashSet<>()
                : statusSet.stream()
                .map(DataListOptionEntity.Status::getId)
                .collect(Collectors.toSet());
    }
}

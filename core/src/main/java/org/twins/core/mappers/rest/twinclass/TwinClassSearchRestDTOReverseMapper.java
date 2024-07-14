package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TwinClassSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSearchRqDTOv1, TwinClassSearch> {

    @Override
    public void map(TwinClassSearchRqDTOv1 src, TwinClassSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertSafe(src.getTwinClassIdExcludeList()))
                .setTwinClassKeyLikeList(convertSafe(src.getTwinClassKeyLikeList()))
                .setHeadTwinClassIdList(convertSafe(src.getHeadTwinClassIdList()))
                .setHeadTwinClassIdExcludeList(convertSafe(src.getHeadTwinClassIdExcludeList()))
                .setExtendsTwinClassIdList(convertSafe(src.getExtendsTwinClassIdList()))
                .setExtendsTwinClassIdExcludeList(convertSafe(src.getExtendsTwinClassIdExcludeList()))
                .setOwnerTypeList(convertSafe(src.getOwnerTypeList()))
                .setOwnerTypeExcludeList(addSystemOwnerTypeToExcludeList(src.getOwnerTypeExcludeList()))
                .setAbstractt(src.getAbstractt())
                .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                .setAliasSpace(src.getAliasSpace());
    }

    private <T> Set<T> convertSafe(List<T> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }

    private Set<TwinClassEntity.OwnerType> addSystemOwnerTypeToExcludeList(List<TwinClassEntity.OwnerType> list) {
        Set<TwinClassEntity.OwnerType> set = convertSafe(list);
        if (set == null) set = new HashSet<>();
        else set = new HashSet<>(set);
        set.add(TwinClassEntity.OwnerType.SYSTEM);
        return set;
    }
}

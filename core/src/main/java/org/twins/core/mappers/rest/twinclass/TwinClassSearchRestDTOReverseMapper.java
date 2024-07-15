package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinClassSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSearchRqDTOv1, TwinClassSearch> {
    @Override
    public void map(TwinClassSearchRqDTOv1 src, TwinClassSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassKeyLikeList(convertToSetSafe(src.getTwinClassKeyLikeList()))
                .setHeadTwinClassIdList(convertToSetSafe(src.getHeadTwinClassIdList()))
                .setExtendsTwinClassIdList(convertToSetSafe(src.getExtendsTwinClassIdList()))
                .setOwnerType(src.getOwnerType())
                .setAbstractt(src.getAbstractt())
                .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                .setAliasSpace(src.getAliasSpace());
    }
}

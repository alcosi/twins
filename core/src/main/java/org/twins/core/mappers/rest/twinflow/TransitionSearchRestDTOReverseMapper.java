package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.search.TransitionSearch;
import org.twins.core.dto.rest.twinflow.TransitionSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionMode.class)
public class TransitionSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionSearchRqDTOv1, TransitionSearch> {

    @Override
    public void map(TransitionSearchRqDTOv1 src, TransitionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(src.twinClassIdList)
                .setTwinClassIdExcludeList(src.twinClassIdExcludeList)
                .setTwinflowIdList(src.twinflowIdList)
                .setTwinflowIdExcludeList(src.twinflowIdExcludeList)
                .setSrcStatusIdList(src.srcStatusIdList)
                .setSrcStatusIdExcludeList(src.srcStatusIdExcludeList)
                .setDstStatusIdList(src.dstStatusIdList)
                .setDstStatusIdExcludeList(src.dstStatusIdExcludeList)
                .setNameLikeList(src.nameLikeList)
                .setPermissionIdList(src.permissionIdList)
                .setPermissionIdExcludeList(src.permissionIdExcludeList)
                .setInbuiltTwinFactoryIdList(src.inbuiltTwinFactoryIdList)
                .setInbuiltTwinFactoryIdExcludeList(src.inbuiltTwinFactoryIdExcludeList)
                .setDraftingTwinFactoryIdList(src.draftingTwinFactoryIdList)
                .setDraftingTwinFactoryIdExcludeList(src.draftingTwinFactoryIdExcludeList);
    }
}

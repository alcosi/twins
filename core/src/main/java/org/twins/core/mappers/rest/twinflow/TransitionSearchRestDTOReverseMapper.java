package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TransitionSearch;
import org.twins.core.dto.rest.twinflow.TransitionSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TransitionSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionSearchRqDTOv1, TransitionSearch> {

    @Override
    public void map(TransitionSearchRqDTOv1 src, TransitionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setTwinClassIdMap(src.getTwinClassIdMap())
                .setTwinClassIdExcludeMap(src.getTwinClassIdExcludeMap())
                .setTwinflowIdList(src.getTwinflowIdList())
                .setTwinflowIdExcludeList(src.getTwinflowIdExcludeList())
                .setSrcStatusIdList(src.getSrcStatusIdList())
                .setSrcStatusIdExcludeList(src.getSrcStatusIdExcludeList())
                .setDstStatusIdList(src.getDstStatusIdList())
                .setDstStatusIdExcludeList(src.getDstStatusIdExcludeList())
                .setAliasLikeList(src.getAliasLikeList())
                .setPermissionIdList(src.getPermissionIdList())
                .setPermissionIdExcludeList(src.getPermissionIdExcludeList())
                .setInbuiltTwinFactoryIdList(src.getInbuiltTwinFactoryIdList())
                .setInbuiltTwinFactoryIdExcludeList(src.getInbuiltTwinFactoryIdExcludeList())
                .setDraftingTwinFactoryIdList(src.getDraftingTwinFactoryIdList())
                .setDraftingTwinFactoryIdExcludeList(src.getDraftingTwinFactoryIdExcludeList())
                .setTwinflowTransitionTypeList(src.getTwinflowTransitionTypeList())
                .setTwinflowTransitionTypeExcludeList(src.getTwinflowTransitionTypeExcludeList())
        ;
    }
}

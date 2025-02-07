package org.twins.core.mappers.rest.attachment;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.AttachmentSearch;
import org.twins.core.dto.rest.attachment.AttachmentSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class AttachmentSearchDTOReverseMapper extends RestSimpleDTOMapper<AttachmentSearchRqDTOv1, AttachmentSearch> {
    @Override
    public void map(AttachmentSearchRqDTOv1 src, AttachmentSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setTwinflowTransitionIdList(src.getTwinflowTransitionIdList())
                .setTwinflowTransitionIdExcludeList(src.getTwinflowTransitionIdExcludeList())
                .setCommentIdList(src.getCommentIdList())
                .setCommentIdExcludeList(src.getCommentIdExcludeList())
                .setTwinClassFieldIdList(src.getTwinClassFieldIdList())
                .setTwinClassFieldIdExcludeList(src.getTwinClassFieldIdExcludeList())
                .setStorageLinkLikeList(src.getStorageLinkLikeList())
                .setStorageLinkNotLikeList(src.getStorageLinkNotLikeList())
                .setViewPermissionIdList(src.getViewPermissionIdList())
                .setViewPermissionIdExcludeList(src.getViewPermissionIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setExternalIdLikeList(src.getExternalIdLikeList())
                .setExternalIdNotLikeList(src.getExternalIdNotLikeList())
                .setTitleLikeList(src.getTitleLikeList())
                .setTitleNotLikeList(src.getTitleNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setCreatedAt(src.getCreatedAt());
    }
}

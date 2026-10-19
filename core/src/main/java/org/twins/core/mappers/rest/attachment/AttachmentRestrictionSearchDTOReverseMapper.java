package org.twins.core.mappers.rest.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.AttachmentRestrictionSearch;
import org.twins.core.dto.rest.attachment.AttachmentRestrictionSearchDTOv1;
import org.twins.core.mappers.rest.IntegerRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class AttachmentRestrictionSearchDTOReverseMapper extends RestSimpleDTOMapper<AttachmentRestrictionSearchDTOv1, AttachmentRestrictionSearch> {
    private final IntegerRangeDTOReverseMapper integerRangeDTOReverseMapper;

    @Override
    public void map(AttachmentRestrictionSearchDTOv1 src, AttachmentRestrictionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setMinCountRange(integerRangeDTOReverseMapper.convert(src.getMinCountRange()))
                .setMaxCountRange(integerRangeDTOReverseMapper.convert(src.getMaxCountRange()))
                .setFileSizeMbLimitRange(integerRangeDTOReverseMapper.convert(src.getFileSizeMbLimitRange()))
                .setFileExtensionLimitLikeList(src.getFileExtensionLimitLikeList())
                .setFileExtensionLimitNotLikeList(src.getFileExtensionLimitNotLikeList())
                .setFileNameRegexpLikeList(src.getFileNameRegexpLikeList())
                .setFileNameRegexpNotLikeList(src.getFileNameRegexpNotLikeList());
    }
}

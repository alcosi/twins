package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.featurer.fieldtyper.descriptor.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseRestDTOMapper;
import org.twins.core.mappers.rest.twin.TwinBaseV2RestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldDescriptorRestDTOMapper extends RestSimpleDTOMapper<FieldDescriptor, TwinClassFieldDescriptorDTO> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final UserRestDTOMapper userRestDTOMapper;
    @Lazy
    @Autowired
    TwinBaseV2RestDTOMapper twinBaseV2RestDTOMapper;

    @Override
    public void map(FieldDescriptor src, TwinClassFieldDescriptorDTO dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinClassFieldDescriptorDTO convert(FieldDescriptor fieldDescriptor, MapperContext mapperContext) throws Exception {
        if (fieldDescriptor instanceof FieldDescriptorText textDescriptor)
            return new TwinClassFieldDescriptorTextDTOv1()
                    .regExp(textDescriptor.regExp());
        else if (fieldDescriptor instanceof FieldDescriptorColorPicker colorDescriptor)
            return new TwinClassFieldDescriptorColorHexDTOv1();
        else if (fieldDescriptor instanceof FieldDescriptorDate dateDescriptor)
            return new TwinClassFieldDescriptorDateScrollDTOv1()
                    .pattern(dateDescriptor.pattern());
        else if (fieldDescriptor instanceof FieldDescriptorList listDescriptor)
            if (listDescriptor.dataListId() != null) {
                return new TwinClassFieldDescriptorListLongDTOv1()
                        .supportCustom(listDescriptor.supportCustom())
                        .multiple(listDescriptor.multiple())
                        .dataListId(listDescriptor.dataListId());
            } else {
                return new TwinClassFieldDescriptorListDTOv1()
                        .supportCustom(listDescriptor.supportCustom())
                        .multiple(listDescriptor.multiple())
                        .options(dataListOptionRestDTOMapper.convertList(listDescriptor.options(), new MapperContext().setMode(DataListOptionRestDTOMapper.Mode.SHORT)));
            }
        else if (fieldDescriptor instanceof FieldDescriptorUser userDescriptor)
            if (userDescriptor.userFilterId() != null) {
                return new TwinClassFieldDescriptorUserLongDTOv1()
                        .multiple(userDescriptor.multiple())
                        .userFilterId(userDescriptor.userFilterId());
            } else {
                TwinClassFieldDescriptorUserDTOv1 userFieldDescriptor = new TwinClassFieldDescriptorUserDTOv1()
                        .multiple(userDescriptor.multiple())
                        .users(userRestDTOMapper.convertListPostpone(userDescriptor.validUsers(), mapperContext
                                .setMode(UserRestDTOMapper.Mode.SHORT)
                                .setLazyRelations(mapperContext.isLazyRelations())));
                if (userFieldDescriptor.users == null)
                    userFieldDescriptor.userIdList(userDescriptor.validUsers().stream().map(UserEntity::getId).toList());
                return userFieldDescriptor;
            }
        else if (fieldDescriptor instanceof FieldDescriptorAttachment attachmentDescriptor)
                return new TwinClassFieldDescriptorAttachmentDTOv1()
                        .multiple(attachmentDescriptor.multiple())
                        .extensions(attachmentDescriptor.extensions())
                        .fileSizeMbLimit(attachmentDescriptor.fileSizeMbLimit())
                        .filenameRegExp(attachmentDescriptor.filenameRegExp());
        else if (fieldDescriptor instanceof FieldDescriptorListShared listSharedDescriptor)
            return new TwinClassFieldDescriptorListSharedInHeadDTOv1()
                    .multiple(listSharedDescriptor.isMultiple());
        else if (fieldDescriptor instanceof FieldDescriptorUrl urlDescriptor)
            return new TwinClassFieldDescriptorUrlDTOv1();
        else if (fieldDescriptor instanceof FieldDescriptorLink linkDescriptor)
            if (linkDescriptor.linkId() != null) {
                return new TwinClassFieldDescriptorLinkLongDTOv1()
                        .multiple(linkDescriptor.multiple())
                        .linkId(linkDescriptor.linkId());
            } else {
                return new TwinClassFieldDescriptorLinkDTOv1()
                        .multiple(linkDescriptor.multiple())
                        .dstTwins(twinBaseV2RestDTOMapper.convertList(linkDescriptor.dstTwins(), new MapperContext().setMode(TwinBaseRestDTOMapper.TwinMode.SHORT)));
            }
        return null;
    }
}

package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.featurer.fieldtyper.FieldDescriptorTwinClassList;
import org.twins.core.featurer.fieldtyper.descriptor.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinClassFieldDescriptorRestDTOMapper extends RestSimpleDTOMapper<FieldDescriptor, TwinClassFieldDescriptorDTO> {

    @MapperModePointerBinding(modes = TwinMode.TwinClassFieldDescriptor2TwinMode.class)
    private final TwinRestDTOMapperV2 twinRestDTOMapper;

    @MapperModePointerBinding(modes = DataListOptionMode.TwinClassFieldDescriptor2DataListOptionMode.class)
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinClassFieldDescriptor2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(FieldDescriptor src, TwinClassFieldDescriptorDTO dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinClassFieldDescriptorDTO convert(FieldDescriptor fieldDescriptor, MapperContext mapperContext) throws Exception {
        if (fieldDescriptor instanceof FieldDescriptorText textDescriptor)
            return new TwinClassFieldDescriptorTextDTOv1()
                    .regExp(textDescriptor.regExp())
                    .editorType(textDescriptor.editorType());
        else if (fieldDescriptor instanceof FieldDescriptorSecret passwordDescriptor)
            return new TwinClassFieldDescriptorSecretDTOv1()
                    .regExp(passwordDescriptor.regExp());
        else if (fieldDescriptor instanceof FieldDescriptorColorPicker colorDescriptor)
            return new TwinClassFieldDescriptorColorHexDTOv1();
        else if (fieldDescriptor instanceof FieldDescriptorDate dateDescriptor)
            return new TwinClassFieldDescriptorDateScrollDTOv1()
                    .pattern(dateDescriptor.pattern())
                    .beforeDate(dateDescriptor.beforeDate())
                    .afterDate(dateDescriptor.afterDate());
        else if (fieldDescriptor instanceof FieldDescriptorTimestamp timestampDescriptor)
            return new TwinClassFieldDescriptorTimestampDTOv1()
                    .pattern(timestampDescriptor.pattern())
                    .beforeDate(timestampDescriptor.beforeDate())
                    .afterDate(timestampDescriptor.afterDate());
        else if (fieldDescriptor instanceof FieldDescriptorList listDescriptor)
            if (listDescriptor.options() == null) {
                TwinClassFieldDescriptorListLongDTOv1 listLongFieldDescriptor = new TwinClassFieldDescriptorListLongDTOv1()
                        .supportCustom(listDescriptor.supportCustom())
                        .multiple(listDescriptor.multiple())
                        .dataListId(listDescriptor.dataListId())
                        .dataListOptionIdList(listDescriptor.dataListOptionIdList())
                        .dataListOptionIdExcludeList(listDescriptor.dataListOptionIdExcludeList())
                        .dataListSubsetIdList(listDescriptor.dataListSubsetIdList())
                        .dataListSubsetIdExcludeList(listDescriptor.dataListSubsetIdExcludeList());
                return listLongFieldDescriptor;
            } else {
                TwinClassFieldDescriptorListDTOv1 listFieldDescriptor = new TwinClassFieldDescriptorListDTOv1()
                        .supportCustom(listDescriptor.supportCustom())
                        .multiple(listDescriptor.multiple())
                        .options(dataListOptionRestDTOMapper.convertCollectionPostpone(listDescriptor.options(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinClassFieldDescriptor2DataListOptionMode.SHORT))));
                if (CollectionUtils.isNotEmpty(listDescriptor.options()))
                    listFieldDescriptor.optionIdList(listDescriptor.options().stream().map(DataListOptionEntity::getId).collect(Collectors.toSet()));
                return listFieldDescriptor;
            }
        else if (fieldDescriptor instanceof FieldDescriptorUser userDescriptor)
            if (userDescriptor.userFilterId() != null) {
                return new TwinClassFieldDescriptorUserLongDTOv1()
                        .multiple(userDescriptor.multiple())
                        .userFilterId(userDescriptor.userFilterId());
            } else {
                TwinClassFieldDescriptorUserDTOv1 userFieldDescriptor = new TwinClassFieldDescriptorUserDTOv1()
                        .multiple(userDescriptor.multiple())
                        .users(userRestDTOMapper.convertCollectionPostpone(userDescriptor.validUsers(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinClassFieldDescriptor2UserMode.SHORT))));
                if (userFieldDescriptor.users == null && CollectionUtils.isNotEmpty(userDescriptor.validUsers()))
                    userFieldDescriptor.userIdList(userDescriptor.validUsers().stream().map(UserEntity::getId).toList());
                return userFieldDescriptor;
            }
        else if (fieldDescriptor instanceof FieldDescriptorAttachment attachmentDescriptor)
            return new TwinClassFieldDescriptorAttachmentDTOv1()
                    .minCount(attachmentDescriptor.minCount())
                    .maxCount(attachmentDescriptor.maxCount())
                    .extensions(attachmentDescriptor.extensions())
                    .fileSizeMbLimit(attachmentDescriptor.fileSizeMbLimit())
                    .filenameRegExp(attachmentDescriptor.filenameRegExp());
        else if (fieldDescriptor instanceof FieldDescriptorBoolean booleanDescriptor)
            return new TwinClassFieldDescriptorBooleanDTOv1()
                    .checkboxType(booleanDescriptor.checkboxType())
                    .nullable(booleanDescriptor.nullable());
        else if (fieldDescriptor instanceof FieldDescriptorNumeric numericDescriptor)
            return new TwinClassFieldDescriptorNumericDTOv1()
                    .min(numericDescriptor.min())
                    .max(numericDescriptor.max())
                    .step(numericDescriptor.step())
                    .thousandSeparator(numericDescriptor.thousandSeparator())
                    .decimalSeparator(numericDescriptor.decimalSeparator())
                    .decimalPlaces(numericDescriptor.decimalPlaces());
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
                var ret = new TwinClassFieldDescriptorLinkDTOv1();
                ret
                        .multiple(linkDescriptor.multiple())
                        .dstTwinIds(linkDescriptor.dstTwins().getIdSet());
                twinRestDTOMapper.postpone(linkDescriptor.dstTwins(),
                        mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinClassFieldDescriptor2TwinMode.SHORT)));
                return ret;
            }
        else if (fieldDescriptor instanceof FieldDescriptorI18n i18nDescriptor) {
            return new TwinClassFieldDescriptorI18nDTOv1();
        }
        else if (fieldDescriptor instanceof FieldDescriptorTwinClassList twinClassListDescriptor) {
            return new TwinClassFieldDescriptorTwinClassListDTOv1();
        }
        return null;
    }
}

package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.dto.rest.twinclass.*;
import org.twins.core.featurer.fieldtyper.descriptor.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldDescriptorRestDTOMapper extends RestSimpleDTOMapper<FieldDescriptor, TwinClassFieldDescriptorDTO> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(FieldDescriptor src, TwinClassFieldDescriptorDTO dst, MapperProperties mapperProperties) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinClassFieldDescriptorDTO convert(FieldDescriptor fieldDescriptor, MapperProperties mapperProperties) throws Exception {
        if (fieldDescriptor instanceof FieldDescriptorText text)
            return new TwinClassFieldDescriptorTextDTOv1()
                    .regExp(text.regExp());
        if (fieldDescriptor instanceof FieldDescriptorColorPicker color)
            return new TwinClassFieldDescriptorColorHexDTOv1();
        if (fieldDescriptor instanceof FieldDescriptorDate date)
            return new TwinClassFieldDescriptorDateScrollDTOv1()
                    .pattern(date.pattern());
        if (fieldDescriptor instanceof FieldDescriptorList list)
            if (list.dataListId() != null) {
                return new TwinClassFieldDescriptorListLongDTOv1()
                        .supportCustom(list.supportCustom())
                        .multiple(list.multiple())
                        .dataListId(list.dataListId());
            } else {
                return new TwinClassFieldDescriptorListDTOv1()
                        .supportCustom(list.supportCustom())
                        .multiple(list.multiple())
                        .options(dataListOptionRestDTOMapper.convertList(list.options(), new MapperProperties().setMode(DataListOptionRestDTOMapper.Mode.ID_NAME_ONLY)));
            }
        if (fieldDescriptor instanceof FieldDescriptorUrl url)
            return new TwinClassFieldDescriptorUrlDTOv1();
        return null;
    }
}

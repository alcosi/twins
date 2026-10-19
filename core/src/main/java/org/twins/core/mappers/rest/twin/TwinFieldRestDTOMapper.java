package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapper extends RestSimpleDTOMapper<TwinField, TwinFieldDTOv1> {

    @MapperModePointerBinding(modes = TwinClassFieldMode.TwinField2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    private final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    private final TwinService twinService;

    @Override
    public void map(TwinField src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        twinService.loadViewableFlag(src);
        if (Boolean.FALSE.equals(src.getViewable())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_ACCESS_DENIED, "{} is not viewable", src.getTwinClassField().logNormal());
        }
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        dst.value(twinFieldValueRestDTOMapper.convert(fieldValue));
        if (mapperContext.hasModeButNot(TwinClassFieldMode.TwinField2TwinClassFieldMode.HIDE))
            dst
                    .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.fork().setModeIfNotPresent(TwinClassFieldMode.TwinField2TwinClassFieldMode.SHORT)));
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinField> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        twinService.loadViewableFlag(srcCollection);
    }
}

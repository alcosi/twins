package org.twins.core.mappers.rest.widget;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.card.CardWidgetEntity;
import org.twins.core.dao.widget.WidgetEntity;
import org.twins.core.dto.rest.card.CardWidgetDTOv1;
import org.twins.core.dto.rest.widget.WidgetDTOv1;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class WidgetRestDTOMapper extends RestSimpleDTOMapper<WidgetEntity, WidgetDTOv1> {

    @Override
    public void map(WidgetEntity src, WidgetDTOv1 dst, MapperProperties mapperProperties) throws ServiceException {
        dst
                .id(src.id())
                .key(src.key())
                .name(src.name())
        ;
    }
}

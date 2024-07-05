package org.twins.core.mappers.rest.card;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.card.CardWidgetEntity;
import org.twins.core.dto.rest.card.CardWidgetDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.WidgetMode;
import org.twins.core.mappers.rest.widget.WidgetRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = WidgetMode.class)
public class CardWidgetRestDTOMapper extends RestSimpleDTOMapper<CardWidgetEntity, CardWidgetDTOv1> {

    private final WidgetRestDTOMapper widgetRestDTOMapper;

    @Override
    public void map(CardWidgetEntity src, CardWidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(WidgetMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .layoutPositionKey(src.getCardLayoutPosition().getKey())
                        .inPositionOrder(src.getInPositionOrder())
                        .name(src.getName())
                        .color(src.getColor())
                        .widgetId(src.getWidgetId())
                        .widget(widgetRestDTOMapper.convertOrPostpone(src.getWidget(), mapperContext));
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .widgetId(src.getWidgetId());
                break;
        }

    }

}

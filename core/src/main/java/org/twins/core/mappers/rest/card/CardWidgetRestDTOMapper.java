package org.twins.core.mappers.rest.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.card.CardWidgetEntity;
import org.twins.core.dto.rest.card.CardWidgetDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.widget.WidgetRestDTOMapper;

@Component
@RequiredArgsConstructor
public class CardWidgetRestDTOMapper extends RestSimpleDTOMapper<CardWidgetEntity, CardWidgetDTOv1> {
    final WidgetRestDTOMapper widgetRestDTOMapper;

    @Override
    public void map(CardWidgetEntity src, CardWidgetDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
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

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }
}

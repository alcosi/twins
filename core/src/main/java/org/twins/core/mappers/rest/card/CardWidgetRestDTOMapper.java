package org.twins.core.mappers.rest.card;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.card.CardWidgetEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.card.CardWidgetDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.featurer.fieldtyper.FieldTypeUIDescriptor;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.widget.WidgetRestDTOMapper;

@Component
@RequiredArgsConstructor
public class CardWidgetRestDTOMapper extends RestSimpleDTOMapper<CardWidgetEntity, CardWidgetDTOv1> {
    final WidgetRestDTOMapper widgetRestDTOMapper;

    @Override
    public void map(CardWidgetEntity src, CardWidgetDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        dst
                .id(src.id())
                .layoutPositionKey(src.cardLayoutPosition().getKey())
                .inPositionOrder(src.inPositionOrder())
                .name(src.name())
                .color(src.color())
                .widget(widgetRestDTOMapper.convert(src.widget(), mapperProperties));
    }
}

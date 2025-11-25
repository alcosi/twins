package org.twins.core.mappers.rest.card;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.card.CardEntity;
import org.twins.core.dto.rest.card.CardDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.CardMode;
import org.twins.core.mappers.rest.mappercontext.modes.WidgetMode;
import org.twins.core.service.card.CardService;
import org.twins.core.service.i18n.I18nService;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = CardMode.class)
public class CardRestDTOMapper extends RestSimpleDTOMapper<CardEntity, CardDTOv1> {

    @MapperModePointerBinding(modes = WidgetMode.Card2WidgetMode.class)
    private final CardWidgetRestDTOMapper cardWidgetRestDTOMapper;

    private final I18nService i18nService;
    private final CardService cardService;

    @Override
    public void map(CardEntity src, CardDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(CardMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .name(I18nCacheHolder.addId(src.getNameI18NId()))
                        .layoutKey(src.getCardLayout().getKey())
                        .logo(src.getLogo());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .name(I18nCacheHolder.addId(src.getNameI18NId()));
                break;
        }
        if (!cardWidgetRestDTOMapper.hideMode(mapperContext))
            dst.widgets(cardWidgetRestDTOMapper.convertCollection(cardService.findCardWidgets(src.getId()), mapperContext.forkOnPoint(WidgetMode.Card2WidgetMode.SHORT)));
    }

}

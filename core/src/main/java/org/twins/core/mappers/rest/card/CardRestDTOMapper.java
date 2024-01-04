package org.twins.core.mappers.rest.card;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.card.CardEntity;
import org.twins.core.dto.rest.card.CardDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.card.CardService;


@Component
@RequiredArgsConstructor
public class CardRestDTOMapper extends RestSimpleDTOMapper<CardEntity, CardDTOv1> {
    final I18nService i18nService;
    final CardService cardService;
    final CardWidgetRestDTOMapper cardWidgetRestDTOMapper;

    @Override
    public void map(CardEntity src, CardDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .name(i18nService.translateToLocale(src.getNameI18NId()))
                        .layoutKey(src.getCardLayout().getKey())
                        .logo(src.getLogo());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .name(i18nService.translateToLocale(src.getNameI18NId()));
                break;
        }
        if (!cardWidgetRestDTOMapper.hideMode(mapperContext))
            dst.widgets(cardWidgetRestDTOMapper.convertList(cardService.findCardWidgets(src.getId())));
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

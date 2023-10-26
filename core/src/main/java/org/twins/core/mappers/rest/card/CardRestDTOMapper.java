package org.twins.core.mappers.rest.card;

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
                        .id(src.id())
                        .key(src.key())
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .layoutKey(src.cardLayout().getKey())
                        .logo(src.logo());
                break;
            case SHORT:
                dst
                        .id(src.id())
                        .key(src.key())
                        .name(i18nService.translateToLocale(src.nameI18n()));
                break;
        }
        if (!cardWidgetRestDTOMapper.hideMode(mapperContext))
            dst.widgets(cardWidgetRestDTOMapper.convertList(cardService.findCardWidgets(src.id())));
    }

    public enum Mode implements MapperMode {
        SHORT, DETAILED, HIDE, SHOW_WIDGETS;

        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
        public static final String _SHOW_WIDGETS = "SHOW_WIDGETS";
    }
}

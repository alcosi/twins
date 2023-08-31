package org.twins.core.mappers.rest.card;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.card.CardEntity;
import org.twins.core.dto.rest.card.CardDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.card.CardService;


@Component
@RequiredArgsConstructor
public class CardRestDTOMapper extends RestSimpleDTOMapper<CardEntity, CardDTOv1> {
    final I18nService i18nService;
    final CardService cardService;
    final CardWidgetRestDTOMapper cardWidgetRestDTOMapper;

    @Override
    public void map(CardEntity src, CardDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(CardRestDTOMapper.Mode.DETAILED)) {
            case SHOW_WIDGETS:
                dst.widgets(
                        cardWidgetRestDTOMapper.convertList(
                                cardService.findCardWidgets(src.id())));
            case DETAILED:
                dst
                        .key(src.key())
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .logo(src.logo());
            case ID_ONLY:
                dst
                        .id(src.id());
                break;
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED, SHOW_WIDGETS;
    }
}

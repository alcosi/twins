package org.twins.face.mappers.rest.widget.wt008;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.face.dao.widget.wt008.FaceWT008Entity;
import org.twins.face.dto.rest.widget.wt008.FaceWT008DTOv1;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class FaceWT008RestDTOMapper extends RestSimpleDTOMapper<FaceWT008Entity, FaceWT008DTOv1> {

    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;

    @Override
    public void map(FaceWT008Entity src, FaceWT008DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);

        dst
                .setKey(src.getKey())
                .setLabel(i18nService.translateToLocale(src.getLabelI18nId())) //todo think over batch
                .setShowChart(Objects.requireNonNullElse(src.getShowChart(), true))
                .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "));
    }
}

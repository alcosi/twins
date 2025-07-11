package org.twins.face.mappers.rest.tc.tc002;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.tc.tc002.FaceTC002Entity;
import org.twins.face.dto.rest.tc.tc002.FaceTC002DTOv1;
import org.twins.face.service.tc.FaceTC002Service;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FaceTC002RestDTOMapper extends RestSimpleDTOMapper<FaceTC002Entity, FaceTC002DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceTC002OptionRestDTOMapper faceTC002OptionRestDTOMapper;
    private final FaceTC002Service faceTC002Service;

    @Override
    public void map(FaceTC002Entity src, FaceTC002DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                faceTC002Service.loadOptions(src);
                dst
                        .setKey(src.getKey())
                        .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                        .setSaveButtonLabel(i18nService.translateToLocale(src.getSaveButtonLabelI18nId()))
                        .setHeader(i18nService.translateToLocale(src.getHeaderI18nId()))
                        .setIcon(resourceService.getResourceUri(src.getIconResource()))
                        .setOptions(faceTC002OptionRestDTOMapper.convertCollection(src.getOptions(), mapperContext));
            }
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<FaceTC002Entity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceTC002Service.loadOptions(srcCollection);
    }
}

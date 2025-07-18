package org.twins.face.mappers.rest.tc.tc001;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FaceMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.tc.tc001.FaceTC001Entity;
import org.twins.face.dto.rest.tc.tc001.FaceTC001DTOv1;
import org.twins.face.service.tc.FaceTC001OptionService;
import org.twins.face.service.tc.FaceTC001Service;


@Component
@RequiredArgsConstructor
public class FaceTC001RestDTOMapper extends RestSimpleDTOMapper<FaceTC001Entity, FaceTC001DTOv1> {
    protected final FaceRestDTOMapper faceRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceTC001OptionRestDTOMapper faceTC001OptionRestDTOMapper;
    private final FaceTC001Service faceTC001Service;
    private final FaceTC001OptionService faceTC001OptionService;

    @Override
    public void map(FaceTC001Entity src, FaceTC001DTOv1 dst, MapperContext mapperContext) throws Exception {
        faceRestDTOMapper.map(src.getFace(), dst, mapperContext);
        switch (mapperContext.getModeOrUse(FaceMode.SHORT)) {
            case SHORT -> dst
                    .setKey(src.getKey());
            case DETAILED -> {
                faceTC001Service.loadOptions(src);
                dst
                        .setKey(src.getKey())
                        .setStyleClasses(StringUtils.splitToSet(src.getStyleClasses(), " "))
                        .setSaveButtonLabel(i18nService.translateToLocale(src.getSaveButtonLabelI18nId()))
                        .setHeaderLabel(i18nService.translateToLocale(src.getHeaderI18nId()))
                        .setIcon(resourceService.getResourceUri(src.getIconResource()))
                        .setOptionSelectLabel(i18nService.translateToLocale(src.getOptionSelectI18nId()))
                        .setSketchMode(src.getSketchMode())
                        .setSingleOptionSilentMode(src.getSingleOptionSilentMode())
                        .setOptions(faceTC001OptionRestDTOMapper.convertCollection(faceTC001OptionService.filterVariants(src.getOptions()), mapperContext));
            }
        }
    }
}

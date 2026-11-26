package org.twins.face.mappers.rest.widget.wt003;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.StringUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.face.FaceWidgetRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dto.rest.widget.wt003.FaceWT003DTOv1;
import org.twins.face.service.widget.FaceWT003Service;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FaceWT003RestDTOMapper extends RestSimpleDTOMapper<PointedFace<FaceWT003Entity>, FaceWT003DTOv1> {

    protected final FaceWidgetRestDTOMapper<FaceWT003Entity> faceWidgetRestDTOMapper;
    private final I18nService i18nService;
    private final ResourceService resourceService;
    private final FaceWT003Service faceWT003Service;

    @Override
    public void map(PointedFace<FaceWT003Entity> src, FaceWT003DTOv1 dst, MapperContext mapperContext) throws Exception {
        FaceWT003Entity config = src.getConfig();
        faceWidgetRestDTOMapper.map(src, dst, mapperContext);
        faceWT003Service.loadIconResources(config);
        faceWT003Service.loadSubstitutionFields(config);

        Map<String, String> titleContext = faceWT003Service.createSubstitutionMap(
                config.getSubstitutionFromTwinPointerId(),
                config.getSubstitutionTitleField()
        );
        Map<String, String> messageContext = faceWT003Service.createSubstitutionMap(
                config.getSubstitutionFromTwinPointerId(),
                config.getSubstitutionMessageField()
        );

        dst
                .setLevel(config.getLevel())
                .setTitle(i18nService.translateToLocale(config.getTitleI18nId(), titleContext)) //todo think over batch
                .setMessage(i18nService.translateToLocale(config.getMessageI18nId(), messageContext))
                .setIcon(resourceService.getResourceUri(config.getIconResource()))
                .setStyleClasses(StringUtils.splitToSet(config.getStyleClasses(), " "));
    }

    @Override
    public void beforeCollectionConversion(Collection<PointedFace<FaceWT003Entity>> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        faceWidgetRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        var configs = srcCollection.stream().map(PointedFace::getConfig).toList();
        faceWT003Service.loadIconResources(configs);
        faceWT003Service.loadSubstitutionFields(configs);
    }
}
